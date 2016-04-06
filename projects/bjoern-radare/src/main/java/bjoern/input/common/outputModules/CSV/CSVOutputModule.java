package bjoern.input.common.outputModules.CSV;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import bjoern.nodeStore.Node;
import bjoern.nodeStore.NodeKey;
import bjoern.nodeStore.NodeTypes;
import bjoern.input.common.outputModules.OutputModule;
import bjoern.structures.RootNode;
import bjoern.structures.annotations.Flag;
import bjoern.structures.annotations.VariableOrArgument;
import bjoern.structures.edges.CallRef;
import bjoern.structures.edges.DirectedEdge;
import bjoern.structures.edges.EdgeTypes;
import bjoern.structures.interpretations.BasicBlock;
import bjoern.structures.interpretations.DisassemblyLine;
import bjoern.structures.interpretations.Function;
import bjoern.structures.interpretations.FunctionContent;
import bjoern.structures.interpretations.Instruction;
import bjoern.input.radare.inputModule.creators.RadareInstructionCreator;

public class CSVOutputModule implements OutputModule
{

	Function currentFunction = null;

	@Override
	public void initialize(String outputDir)
	{
		CSVWriter.changeOutputDir(outputDir);
	}

	@Override
	public void finish()
	{
		CSVWriter.finish();
	}

	@Override
	public void writeFlag(Flag flag)
	{
		createRootNodeForNode(flag);

		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(CSVFields.CODE, flag.getValue());
		properties.put(CSVFields.KEY, flag.getKey());
		properties.put(CSVFields.TYPE, flag.getType());
		properties.put(CSVFields.ADDR, flag.getAddress().toString());
		// Skipping length-field for now, let's see if we need it.
		CSVWriter.addNode(flag, properties);
	}

	private void createRootNodeForNode(Node node)
	{
		Node rootNode = new RootNode();
		rootNode.setAddr(node.getAddress());
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(CSVFields.KEY, rootNode.getKey());
		properties.put(CSVFields.ADDR, rootNode.getAddress().toString());
		properties.put(CSVFields.TYPE, rootNode.getType());
		CSVWriter.addNoReplaceNode(rootNode, properties);
	}

	@Override
	public void writeFunctionNodes(Function function)
	{
		createRootNodeForNode(function);

		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(CSVFields.ADDR, function.getAddress().toString());
		properties.put(CSVFields.TYPE, function.getType());
		properties.put(CSVFields.REPR, function.getName());
		properties.put(CSVFields.KEY, function.getKey());

		CSVWriter.addNoReplaceNode(function, properties);
	}

	@Override
	public void writeFunctionContent(Function function)
	{
		setCurrentFunction(function);

		writeArgumentsAndVariables();
		writeBasicBlocks();
		writeCFGEdges();

		setCurrentFunction(null);
	}

	private void writeArgumentsAndVariables()
	{
		FunctionContent content = currentFunction.getContent();
		List<VariableOrArgument> varsAndArgs = content
				.getVariablesAndArguments();

		for (VariableOrArgument varOrArg : varsAndArgs)
		{
			createRootNodeForNode(varOrArg);
			createNodeForVarOrArg(varOrArg);
			addEdgeFromRootNode(varOrArg, EdgeTypes.ANNOTATION);
		}

	}

	private void createNodeForVarOrArg(VariableOrArgument varOrArg)
	{
		Map<String, Object> properties = new HashMap<String, Object>();
		String type = varOrArg.getType();
		if (type.equals(CSVFields.VAR))
			properties.put(CSVFields.TYPE, NodeTypes.LOCAL_VAR);
		else
			properties.put(CSVFields.TYPE, NodeTypes.ARG);

		properties.put(CSVFields.KEY, varOrArg.getKey());
		properties.put(CSVFields.TYPE, varOrArg.getType());
		properties.put(CSVFields.ADDR, varOrArg.getAddress().toString());
		properties.put(CSVFields.NAME, varOrArg.getVarName());
		properties.put(CSVFields.REPR, varOrArg.getVarType());
		properties.put(CSVFields.CODE, varOrArg.getRegPlusOffset());

		CSVWriter.addNode(varOrArg, properties);
	}

	private void setCurrentFunction(Function function)
	{
		currentFunction = function;
	}

	private void writeBasicBlocks()
	{
		Function function = currentFunction;

		Collection<BasicBlock> basicBlocks = function.getContent()
				.getBasicBlocks();
		for (BasicBlock block : basicBlocks)
		{
			writeBasicBlock(block);
			writeEdgeFromFunctionToBasicBlock(function, block);
		}
	}

	private void writeEdgeFromFunctionToBasicBlock(Function function, BasicBlock block)
	{

		Map<String, Object> properties = new HashMap<String, Object>();

		String srcId = function.getKey();
		String dstId = block.getKey();

		CSVWriter.addEdge(srcId, dstId, properties, EdgeTypes.IS_FUNCTION_OF);

	}

	@Override
	public void writeBasicBlock(BasicBlock block)
	{
		createRootNodeForNode(block);
		writeNodeForBasicBlock(block);
		addEdgeFromRootNode(block, EdgeTypes.INTERPRETATION);
		writeInstructions(block);
	}

	private void writeInstructions(BasicBlock block)
	{
		Collection<Instruction> instructions = block.getInstructions();
		Iterator<Instruction> it = instructions.iterator();


		int childNum = 0;
		Instruction instr;
		Instruction prevInstr = null;
		while (it.hasNext())
		{
			instr = it.next();
			createRootNodeForNode(instr);
			writeInstruction(instr, childNum);
			addEdgeFromRootNode(instr, EdgeTypes.INTERPRETATION);
			if(prevInstr != null)
				addEdgeFromPreviousInstruction(instr, prevInstr);

			writeEdgeFromBlockToInstruction(block, instr);
			childNum++;
			prevInstr = instr;
		}

	}

	private void addEdgeFromPreviousInstruction(Instruction instr, Instruction prevInstr)
	{
		Map<String, Object> properties = new HashMap<String, Object>();

		String srcId = prevInstr.getKey();
		String dstId = instr.getKey();

		CSVWriter.addEdge(srcId, dstId, properties, EdgeTypes.IS_NEXT_IN_BB);
	}

	private void writeEdgeFromBlockToInstruction(BasicBlock block,
			Instruction instr)
	{
		Map<String, Object> properties = new HashMap<String, Object>();

		String srcId = block.getKey();
		String dstId = instr.getKey();

		CSVWriter.addEdge(srcId, dstId, properties, EdgeTypes.IS_BB_OF);
	}

	private void writeInstruction(Instruction instr,
			int childNum)
	{
		Map<String, Object> properties = new HashMap<String, Object>();

		Long instrAddress = instr.getAddress();

		properties.put(CSVFields.ADDR, instrAddress.toString());
		properties.put(CSVFields.TYPE, instr.getType());
		properties.put(CSVFields.REPR, instr.getStringRepr());
		properties.put(CSVFields.CHILD_NUM, String.format("%d", childNum));
		properties.put(CSVFields.KEY, instr.getKey());
		properties.put(CSVFields.CODE,instr.getBytes());

		addDisassemblyProperties(properties, instrAddress);

		CSVWriter.addNode(instr, properties);
	}

	private void addDisassemblyProperties(Map<String, Object> properties,
			Long address)
	{
		FunctionContent content = currentFunction.getContent();
		if (content == null)
			return;
		DisassemblyLine line = content.getDisassemblyLineForAddr(address);
		if (line == null)
			return;

		properties.put(CSVFields.COMMENT, line.getComment());

		DisassemblyLine esilLine = content.getDisassemblyEsilLineForAddr(address);
		if (esilLine == null)
			return;

		properties.put(CSVFields.ESIL, esilLine.getInstruction());

	}

	private void writeNodeForBasicBlock(BasicBlock block)
	{
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(CSVFields.ADDR, block.getAddress().toString());
		properties.put(CSVFields.TYPE, block.getType());
		properties.put(CSVFields.KEY, block.getKey());
		properties.put(CSVFields.REPR, block.getInstructionsStr());

		CSVWriter.addNode(block, properties);
	}

	private void writeCFGEdges()
	{
		Function function = currentFunction;
		List<DirectedEdge> edges = function.getContent().getEdges();
		for (DirectedEdge edge : edges)
		{

			NodeKey from = edge.getSourceKey();
			NodeKey to = edge.getDestKey();
			String srcId = from.toString();
			String dstId = to.toString();

			Map<String, Object> properties = new HashMap<String, Object>();
			String edgeType = edge.getType();
			CSVWriter.addEdge(srcId, dstId, properties, edgeType);
		}
	}

	@Override
	public void writeReferencesToFunction(Function function)
	{
		addEdgeFromRootNode(function, EdgeTypes.INTERPRETATION);
	}

	private void writeEdge(DirectedEdge edge)
	{

		String sourceKey = edge.getSourceKey().toString();
		String destKey = edge.getDestKey().toString();
		String type = edge.getType();
		Map<String, Object> properties = new HashMap<String, Object>();
		// TODO: add edge properties.
		CSVWriter.addEdge(sourceKey, destKey, properties, type);
	}

	@Override
	public void attachFlagsToRootNodes(Flag flag)
	{
		addEdgeFromRootNode(flag, EdgeTypes.ANNOTATION);
	}

	private void addEdgeFromRootNode(Node node, String type)
	{
		NodeKey srcKey = node.createEpsilonKey();
		NodeKey destKey = node.createKey();

		DirectedEdge newEdge = new DirectedEdge();
		newEdge.setSourceKey(srcKey);
		newEdge.setDestKey(destKey);
		newEdge.setType(type);

		writeEdge(newEdge);
	}

	public void writeCrossReference(DirectedEdge xref)
	{
		writeSourceNode(xref);
		writeEdge(xref);
	}

	private void writeSourceNode(DirectedEdge xref)
	{
		if(!(xref instanceof CallRef))
			return;

		CallRef callRef = (CallRef) xref;
		DisassemblyLine disassemblyLine = callRef.getDisassemblyLine();

		Instruction instruction = RadareInstructionCreator.createFromDisassemblyLine(disassemblyLine);

		Map<String, Object> properties = new HashMap<String, Object>();

		Long instrAddress = callRef.getSourceKey().getAddress();

		properties.put(CSVFields.ADDR, instrAddress.toString());
		properties.put(CSVFields.TYPE, instruction.getType());
		properties.put(CSVFields.REPR, instruction.getStringRepr());
		properties.put(CSVFields.KEY, instruction.getKey());
		properties.put(CSVFields.CODE,instruction.getBytes());
		properties.put(CSVFields.COMMENT, disassemblyLine.getComment());

		CSVWriter.addNode(instruction, properties);

	}

}
