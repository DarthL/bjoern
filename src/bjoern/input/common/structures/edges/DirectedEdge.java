package bjoern.input.common.structures.edges;

import bjoern.input.common.nodeStore.NodeKey;

public class DirectedEdge
{
	NodeKey sourceKey;
	NodeKey destKey;


	String type;

	public NodeKey getSourceKey()
	{
		return sourceKey;
	}

	public void setSourceKey(NodeKey sourceKey)
	{
		this.sourceKey = sourceKey;
	}

	public NodeKey getDestKey()
	{
		return destKey;
	}

	public void setDestKey(NodeKey destKey)
	{
		this.destKey = destKey;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}
}
