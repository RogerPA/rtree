package com.roger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import com.google.appengine.api.datastore.Entity;


public class RTree {

  private final static int maxEntries = 4;
  private final static int minEntries = 2;
  private final static int numDims = 2;
  private static final String Rtree = null;

  private static Node root = buildRoot(true);

  private volatile static int size;

  /**
   * Creates a new RTree.
   * 
   * @param maxEntries
   *          maximum number of entries per node
   * @param minEntries
   *          minimum number of entries per node (except for the root node)
   * @param numDims
   *          the number of dimensions of the RTree.
   */
  private static Node buildRoot(boolean asLeaf)
  {
    float[] initCoords = new float[numDims];
    float[] initDimensions = new float[numDims];
    for (int i = 0; i < numDims; i++)
    {
      initCoords[i] = (float) Math.sqrt(Float.MAX_VALUE);
      initDimensions[i] = -2.0f * (float) Math.sqrt(Float.MAX_VALUE);
    }
    return new Node(initCoords, initDimensions, asLeaf);
  }

  /**
   * Builds a new RTree using default parameters: maximum 50 entries per node
   * minimum 2 entries per node 2 dimensions
   */
  /**
   * @return the maximum number of entries per node
   */
  public int getMaxEntries()
  {
    return maxEntries;
  }

  /**
   * @return the minimum number of entries per node for all nodes except the
   *         root.
   */
  public int getMinEntries()
  {
    return minEntries;
  }

  /**
   * @return the number of dimensions of the tree
   */
  public int getNumDims()
  {
    return numDims;
  }

  /**
   * @return the number of items in this tree.
   */
  public static int size()
  {
    return size;
  }

  /**
   * Searches the RTree for objects overlapping with the given rectangle.
   * 
   * @param coords
   *          the corner of the rectangle that is the lower bound of every
   *          dimension (eg. the top-left corner)
   * @param dimensions
   *          the dimensions of the rectangle.
   * @return a list of objects whose rectangles overlap with the given
   *         rectangle.
   */
  public static List<Entity> search(float[] coords, float[] dimensions)
  {
    assert (coords.length == numDims);
    assert (dimensions.length == numDims);
    LinkedList<Entity> results = new LinkedList<Entity>();
    search(coords, dimensions, root, results);
    return results;
  }


private static void search(float[] coords, float[] dimensions, Node n,
      LinkedList<Entity> results)
  {
    if (n.leaf)
    {
      for (Node e : n.children)
      {
        if (isOverlap(coords, dimensions, e.coords, e.dimensions))
        {
          results.add(((Entry) e).entry);
        }
      }
    }
    else
    {
      for (Node c : n.children)
      {
        if (isOverlap(coords, dimensions, c.coords, c.dimensions))
        {
          search(coords, dimensions, c, results);
        }
      }
    }
  }

  /**
   * Deletes the entry associated with the given rectangle from the RTree
   * 
   * @param coords
   *          the corner of the rectangle that is the lower bound in every
   *          dimension
   * @param dimensions
   *          the dimensions of the rectangle
   * @param entry
   *          the entry to delete
   * @return true iff the entry was deleted from the RTree.
   */
  public boolean delete(float[] coords, float[] dimensions, Entity entry)
  {
    assert (coords.length == numDims);
    assert (dimensions.length == numDims);
    Node l = findLeaf(root, coords, dimensions, entry);
    assert (l.leaf);
    // TODO mi aporte =)
    if (l == null) {
    	return false;
    }
    ListIterator<Node> li = l.children.listIterator();
    Entity removed = null;
    while (li.hasNext())
    {
      Entry e = (Entry) li.next();
      if (e.entry.equals(entry) && isOverlap(e.coords, e.dimensions, coords, dimensions))
      {
        removed = e.entry;
        li.remove();
        break;
      }
    }
    if (removed != null)
    {
      condenseTree(l);
      size--;
    }
    if ( size == 0 )
    {
      root = buildRoot(true);
    }
    return (removed != null);
  }

  private Node findLeaf(Node n, float[] coords, float[] dimensions, Entity entry)
  {
    if (n.leaf)
    {
    	return n;
      /*for (Node c : n.children)
      {
    	  // TODO mi aporte =)
        if (isOverlap(c.coords, c.dimensions, coords, dimensions) && ((Entry) c).entry.equals(entry))
        {
          return n;
        }
      }
      return null;*/
    }
    else
    {
      for (Node c : n.children)
      {
        if (isOverlap(c.coords, c.dimensions, coords, dimensions))
        {
          Node result = findLeaf(c, coords, dimensions, entry);
          if (result != null)
          {
            return result;
          }
        }
      }
      return null;
    }
  }

  private void condenseTree(Node n)
  {
    Set<Node> q = new HashSet<Node>();
    while (n != root)
    {
      if (n.leaf && (n.children.size() < minEntries))
      {
        q.addAll(n.children);
        n.parent.children.remove(n);
      }
      else if (!n.leaf && (n.children.size() < minEntries))
      {
        // probably a more efficient way to do this...
        LinkedList<Node> toVisit = new LinkedList<Node>(n.children);
        while (!toVisit.isEmpty())
        {
          Node c = toVisit.pop();
          if (c.leaf)
          {
            q.addAll(c.children);
          }
          else
          {
            toVisit.addAll(c.children);
          }
        }
        n.parent.children.remove(n);
      }
      else
      {
        tighten(n);
      }
      n = n.parent;
    }
    for (Node ne : q)
    {
      Entry e = (Entry) ne;
      insert(e.coords, e.dimensions, e.entry);
    }
  }

  /**
   * Inserts the given entry into the RTree, associated with the given
   * rectangle.
   * 
   * @param coords
   *          the corner of the rectangle that is the lower bound in every
   *          dimension
   * @param dimensions
   *          the dimensions of the rectangle
   * @param entry
   *          the entry to insert
   */
  public static void insert(float[] coords, float[] dimensions, Entity entry)
  {
    assert (coords.length == numDims);
    assert (dimensions.length == numDims);
    Entry e = new Entry(coords, dimensions, entry);
    Node l = chooseLeaf(root, e);
    l.children.add(e);
    size++;
    e.parent = l;
    if (l.children.size() > maxEntries)
    {
      Node[] splits = splitNode(l);
      adjustTree(splits[0], splits[1]);
    }
    else
    {
      adjustTree(l, null);
    }
  }

  /**
   * Empties the RTree
   */
  public void clear()
  {
    root = buildRoot(true);
    // let the GC take care of the rest.
  }
  
  private static void adjustTree(Node n, Node nn)
  {
    if (n == root)
    {
      if (nn != null)
      {
        // build new root and add children.
        root = buildRoot(false);
        root.children.add(n);
        n.parent = root;
        root.children.add(nn);
        nn.parent = root;
      }
      tighten(root);
      return;
    }
    tighten(n);
    if (nn != null)
    {
      tighten(nn);
      if (n.parent.children.size() > maxEntries)
      {
        Node[] splits = splitNode(n.parent);
        adjustTree(splits[0], splits[1]);
      }
    }
    else if (n.parent != null)
    {
      adjustTree(n.parent, null);
    }
  }

  private static Node[] splitNode(Node n)
  {
    Node[] nn = new Node[] { n, new Node(n.coords, n.dimensions, n.leaf) };
    nn[1].parent = n.parent;
    if (nn[1].parent != null)
    {
      nn[1].parent.children.add(nn[1]);
    }
    LinkedList<Node> cc = new LinkedList<Node>(n.children);
    n.children.clear();
    Node[] ss = pickSeeds(cc);
    nn[0].children.add(ss[0]);
    nn[1].children.add(ss[1]);
    while (!cc.isEmpty())
    {
      if ((nn[0].children.size() >= minEntries)
          && (nn[1].children.size() + cc.size() == minEntries))
      {
        nn[1].children.addAll(cc);
        cc.clear();
        return nn;
      }
      else if ((nn[1].children.size() >= minEntries)
          && (nn[0].children.size() + cc.size() == minEntries))
      {
        nn[0].children.addAll(cc);
        cc.clear();
        return nn;
      }
      Node c = cc.pop();
      Node preferred;
      // Implementation of linear PickNext
      float e0 = getRequiredExpansion(nn[0].coords, nn[0].dimensions, c);
      float e1 = getRequiredExpansion(nn[1].coords, nn[1].dimensions, c);
      if (e0 < e1)
      {
        preferred = nn[0];
      }
      else if (e0 > e1)
      {
        preferred = nn[1];
      }
      else
      {
        float a0 = getArea(nn[0].dimensions);
        float a1 = getArea(nn[1].dimensions);
        if (a0 < a1)
        {
          preferred = nn[0];
        }
        else if (e0 > a1)
        {
          preferred = nn[1];
        }
        else
        {
          if (nn[0].children.size() < nn[1].children.size())
          {
            preferred = nn[0];
          }
          else if (nn[0].children.size() > nn[1].children.size())
          {
            preferred = nn[1];
          }
          else
          {
            preferred = nn[(int) Math.round(Math.random())];
          }
        }
      }
      preferred.children.add(c);
    }
    tighten(nn[0]);
    tighten(nn[1]);
    return nn;
  }

  // Implementation of LinearPickSeeds
  private static Node[] pickSeeds(LinkedList<Node> nn)
  {
    Node[] bestPair = null;
    float bestSep = 0.0f;
    for (int i = 0; i < numDims; i++)
    {
      float dimLb = Float.MAX_VALUE, dimMinUb = Float.MAX_VALUE;
      float dimUb = -1.0f * Float.MAX_VALUE, dimMaxLb = -1.0f * Float.MAX_VALUE;
      Node nMaxLb = null, nMinUb = null;
      for (Node n : nn)
      {
        if (n.coords[i] < dimLb)
        {
          dimLb = n.coords[i];
        }
        if (n.dimensions[i] + n.coords[i] > dimUb)
        {
          dimUb = n.dimensions[i] + n.coords[i];
        }
        if (n.coords[i] > dimMaxLb)
        {
          dimMaxLb = n.coords[i];
          nMaxLb = n;
        }
        if (n.dimensions[i] + n.coords[i] < dimMinUb)
        {
          dimMinUb = n.dimensions[i] + n.coords[i];
          nMinUb = n;
        }
      }
      float sep = (nMaxLb == nMinUb) ? -1.0f :
                  Math.abs((dimMinUb - dimMaxLb) / (dimUb - dimLb));
      if (sep >= bestSep)
      {
        bestPair = new Node[]
        { nMaxLb, nMinUb };
        bestSep = sep;
      }
    }
    // In the degenerate case where all points are the same, the above
    // algorithm does not find a best pair.  Just pick the first 2
    // children.
    if ( bestPair == null )
    {
      bestPair = new Node[] { nn.get(0), nn.get(1) };
    }
    nn.remove(bestPair[0]);
    nn.remove(bestPair[1]);
    return bestPair;
  }

  private static void tighten(Node n)
  {
    float[] minCoords = new float[n.coords.length];
    float[] maxDimensions = new float[n.dimensions.length];
    for (int i = 0; i < minCoords.length; i++)
    {
      minCoords[i] = Float.MAX_VALUE;
      maxDimensions[i] = 0.0f;

      for (Node c : n.children)
      {
        // we may have bulk-added a bunch of children to a node (eg. in
        // splitNode)
        // so here we just enforce the child->parent relationship.
        c.parent = n;
        if (c.coords[i] < minCoords[i])
        {
          minCoords[i] = c.coords[i];
        }
        if ((c.coords[i] + c.dimensions[i]) > maxDimensions[i])
        {
          maxDimensions[i] = (c.coords[i] + c.dimensions[i]);
        }
      }
    }
    System.arraycopy(minCoords, 0, n.coords, 0, minCoords.length);
    System.arraycopy(maxDimensions, 0, n.dimensions, 0, maxDimensions.length);
  }

  private static Node chooseLeaf(Node n, Entry e)
  {
    if (n.leaf)
    {
      return n;
    }
    float minInc = Float.MAX_VALUE;
    Node next = null;
    for (Node c : n.children)
    {
      float inc = getRequiredExpansion(c.coords, c.dimensions, e);
      if (inc < minInc)
      {
        minInc = inc;
        next = c;
      }
      else if (inc == minInc)
      {
        float curArea = 1.0f;
        float thisArea = 1.0f;
        for (int i = 0; i < c.dimensions.length; i++)
        {
          curArea *= next.dimensions[i];
          thisArea *= c.dimensions[i];
        }
        if (thisArea < curArea)
        {
          next = c;
        }
      }
    }
    return chooseLeaf(next, e);
  }

  /**
   * Returns the increase in area necessary for the given rectangle to cover the
   * given entry.
   */
  private static float getRequiredExpansion(float[] coords, float[] dimensions, Node e)
  {
    float area = getArea(dimensions);
    float[] deltas = new float[dimensions.length];
    for (int i = 0; i < deltas.length; i++)
    {
      if (coords[i] + dimensions[i] < e.coords[i] + e.dimensions[i])
      {
        deltas[i] = e.coords[i] + e.dimensions[i] - coords[i] - dimensions[i];
      }
      else if (coords[i] + dimensions[i] > e.coords[i] + e.dimensions[i])
      {
        deltas[i] = coords[i] - e.coords[i];
      }
    }
    float expanded = 1.0f;
    for (int i = 0; i < dimensions.length; i++)
    {
      area *= dimensions[i] + deltas[i];
    }
    return (expanded - area);
  }

  private static float getArea(float[] dimensions)
  {
    float area = 1.0f;
    for (int i = 0; i < dimensions.length; i++)
    {
      area *= dimensions[i];
    }
    return area;
  }

  private static boolean isOverlap(float[] scoords, float[] sdimensions,
      float[] coords, float[] dimensions)
  {
    for (int i = 0; i < scoords.length; i++)
    {
      boolean overlapInThisDimension = false;
      if (scoords[i] == coords[i])
      {
        overlapInThisDimension = true;
      }
      else if (scoords[i] < coords[i])
      {
        if (scoords[i] + sdimensions[i] >= coords[i])
        {
          overlapInThisDimension = true;
        }
      }
      else if (scoords[i] > coords[i])
      {
        if (coords[i] + dimensions[i] >= scoords[i])
        {
          overlapInThisDimension = true;
        }
      }
      if (!overlapInThisDimension)
      {
        return false;
      }
    }
    return true;
  }
 
  //TODO Node should extend an entity so each root node will have entity descendants 
  

  

  // The methods below this point can be used to create an HTML rendering
  // of the RTree.  Maybe useful for debugging?
  
  private static final int elemWidth = 150;
  private static final int elemHeight = 120;
  
  String serialize()
  {
    int ubDepth = (int)Math.ceil(Math.log(size)/Math.log(minEntries)) * elemHeight;
    int ubWidth = size * elemWidth;
    java.io.StringWriter sw = new java.io.StringWriter();
    java.io.PrintWriter pw = new java.io.PrintWriter(sw);
    //pw.println( "<html><head></head><body>");
    pw.println( "Aqui");
    serialize(root, pw, 0, 0, ubWidth, ubDepth);
    pw.println( "</body>");
    pw.flush();
    return sw.toString();
  }
  
  private void serialize(Node n, java.io.PrintWriter pw, int x0, int y0, int w, int h)
  {
    pw.printf( "<div style=\"position:absolute; left: %d; top: %d; width: %d; height: %d; border: 1px dashed\">\n",
               x0, y0, w, h);
    pw.println( "<pre>");
    pw.println( "Node: " + n.toString() + " (root==" + (n == root) + ") \n" );
    pw.println( "Coords: " + Arrays.toString(n.coords) + "\n");
    pw.println( "Dimensions: " + Arrays.toString(n.dimensions) + "\n");
    pw.println( "# Children: " + ((n.children == null) ? 0 : n.children.size()) + "\n" );
    pw.println( "isLeaf: " + n.leaf + "\n");
    pw.println( "</pre>");
    int numChildren = (n.children == null) ? 0 : n.children.size();
    for ( int i = 0; i < numChildren; i++ )
    {
    	serialize(n.children.get(i), pw, (int)(x0 + (i * w/(float)numChildren)),
          y0 + elemHeight, (int)(w/(float)numChildren), h - elemHeight);
    }
    pw.println( "</div>" );
  }
}