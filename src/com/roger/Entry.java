package com.roger;

import com.google.appengine.api.datastore.Entity;

class Entry extends Node
{
  final Entity entry;

  public Entry(float[] coords, float[] dimensions, Entity entry)
  {
    // an entry isn't actually a leaf (its parent is a leaf)
    // but all the algorithms should stop at the first leaf they encounter,
    // so this little hack shouldn't be a problem.
    super(coords, dimensions, true);
    this.entry = entry;
  }

  public String toString()
  {
    return "Entry: " + entry;
  }
}