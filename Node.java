
public class Node {

	int degree;
	long data;
	Node leftSibling, rightSibling, parent, child;
	boolean childCut;
	
	public Node(long data) {
		this.data = data;
		parent = child = null;
		leftSibling = rightSibling = this;
		degree = 0;
		childCut = false;
	}
	
	public void isolateNode(){				//	Before removing maxNode, isolate it
		this.rightSibling = this.leftSibling = this;
		this.child = null;
		this.degree = 0;
	}
	
	public void makeChildOf(Node parent){	//	Add this node as the leftmost(first) child
		if(parent.child != null) {			//	If parent has child(ren)
			this.rightSibling = parent.child;
			this.leftSibling = parent.child.leftSibling;
			this.rightSibling.leftSibling = this;
			this.leftSibling.rightSibling = this;
		}
		else								//	If parent has no child
			this.rightSibling = this.leftSibling = this;
		parent.child = this;
		this.parent = parent;
		this.childCut = false;
	}
}
