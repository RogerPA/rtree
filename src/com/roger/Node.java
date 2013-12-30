package com.roger;

import java.util.LinkedList;

class Node
{
	  /*Key nodeKey = KeyFactory.createKey("Node", this.getNodeName());
    String content = req.getParameter("content");
    Date date = new Date();
    
    Entity node = new Entity("Node", guestbookKey);
    node.setProperty("user", user);
    node.setProperty("date", date);
    node.setProperty("content", content);*/
    
  final float[] coords;
  final float[] dimensions;
  final LinkedList<Node> children;
  final boolean leaf;

  Node parent;

  Node(float[] coords, float[] dimensions, boolean leaf)
  {
    this.coords = new float[coords.length];
    this.dimensions = new float[dimensions.length];
    System.arraycopy(coords, 0, this.coords, 0, coords.length);
    System.arraycopy(dimensions, 0, this.dimensions, 0, dimensions.length);
    this.leaf = leaf;
    children = new LinkedList<Node>();
  }

}