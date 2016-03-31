package bjoern.input.common.structures.interpretations;

import bjoern.input.common.nodeStore.Node;
import bjoern.input.common.nodeStore.NodeTypes;


public class Function extends Node
{

	FunctionContent content;

	private String name = "";

	public Function(long addr)
	{
		content = new FunctionContent(addr);
		setType(NodeTypes.FUNCTION);
		setAddr(addr);
	}

	public FunctionContent getContent()
	{
		return content;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setContent(FunctionContent content)
	{
		this.content = content;
	}

	public void deleteContent()
	{
		content = null;
	}

}
