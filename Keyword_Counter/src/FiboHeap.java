import java.util.Hashtable;

public class FiboHeap {

	Node maxNode = null;
	
	private void updateMaxNode(Node firstNode) {
		maxNode = firstNode;					//	Initialize maxNode as current node
		Node currNode = firstNode.rightSibling;
		while(currNode != firstNode) {			//	Traverse through all nodes
			if(currNode.data >= maxNode.data)	// 	>= so that original maxNode(if not null) stays maxNode
				maxNode = currNode;
			currNode = currNode.rightSibling;
		}
	}
	
	public void insert(Node newNode) {
		if(maxNode != null) {					//	Insert newNode as right sibling of maxNode
			newNode.rightSibling = maxNode.rightSibling;
			newNode.leftSibling = maxNode;
			newNode.leftSibling.rightSibling = newNode;
			newNode.rightSibling.leftSibling = newNode;
			updateMaxNode(newNode);
		}
		else
			maxNode = newNode;					// newNode is first node in the heap
	}
	
	private void makeSiblingsParentNull(Node firstNode) {	// set parent pointer of all children of maxNode to null while removing maxNode
		firstNode.parent = null;
		Node nextNode = firstNode.rightSibling;
		while(nextNode != firstNode){			// Traverse through all siblings of firstNode
			nextNode.parent = null;
			nextNode = nextNode.rightSibling;
		} 
	}
	
	public Node removeMax() {
		if(maxNode == null)
			return null;			// Heap is empty
		Node retNode = null;		// retNode will point to the maxNode as maxNode pointer will be set to null
		Node tempLeft = maxNode.leftSibling;
		Node tempRight = maxNode.rightSibling;
		Node firstChild = maxNode.child;
		if(tempLeft == maxNode && tempRight == maxNode) {		// Check if maxNode's is the only maxTree in the heap
			retNode = maxNode;
			if(firstChild == null)	//	Check if maxNode is no child
				maxNode = null;		//	Removing maxNode would make the heap empty
			else{					//	maxNode has 1+ child
				makeSiblingsParentNull(firstChild);
				maxNode = null;
				retNode.isolateNode();			// Isolate maxNode before returning
				pairwiseCombine(firstChild);	// pairwiseCombine starting from maxNode's first child
			}
		}
		else {						//	maxNode has 1+ sibling
			if(firstChild != null) {				//	Check if it has 1+ child.
				makeSiblingsParentNull(firstChild);
				Node lastChild = firstChild.leftSibling;
				tempLeft.rightSibling = firstChild;
				firstChild.leftSibling = tempLeft;
				lastChild.rightSibling = tempRight;
				tempRight.leftSibling = lastChild;
			}
			else {					// Connect maxNode's left and right siblings
				tempLeft.rightSibling = tempRight;
				tempRight.leftSibling = tempLeft;
			}
			retNode = maxNode;
			maxNode = null;
			retNode.isolateNode();
			pairwiseCombine(tempRight);	// pairwiseCombine starting from maxNode's right sibling
		}
		return retNode;				//	Return the isolated maxNode
	}
	
	private void pairwiseCombine(Node firstNode) {			// First and also the terminating node in the procedure.
		Hashtable<Integer, Node> pcTable = new Hashtable<Integer, Node>();		// Hashtable maintains <degree, node> entries
		Node currNode = firstNode;
		Node otherNode = null;
		while(true) {
			//	Check if currNode's degree has an entry in hashtable. If it has, check if it's non-null.
			if(pcTable.containsKey(currNode.degree) && pcTable.get(currNode.degree) != null){
				otherNode = pcTable.get(currNode.degree);	//	otherNode is the node from the hashtable
				otherNode.leftSibling.rightSibling = otherNode.rightSibling;	// Detach otherNode from the sibling list
				otherNode.rightSibling.leftSibling = otherNode.leftSibling;		// Because we will bring otherNode to currNode's place
				if(otherNode.data <= currNode.data){
					if(firstNode == otherNode)				
						firstNode = otherNode.rightSibling;	// Update firstNode pointer
					otherNode.makeChildOf(currNode);	//	Make otherNode child of currNode		
					pcTable.remove(otherNode.degree);	//	Remove otherNode's entry
					++currNode.degree;
				}
				else {										//	currNode is smaller than otherNode
					if(currNode.rightSibling == currNode)	//	If currNode has no siblings other than otherNode
						otherNode.rightSibling = otherNode.leftSibling = otherNode;	// Self loop otherNode
					else {									// currNode has other siblings
						otherNode.rightSibling = currNode.rightSibling;		//	Connect otherNode and currNode's siblings
						otherNode.leftSibling = currNode.leftSibling;		// We are bringing otherNode in place of currNode
						otherNode.rightSibling.leftSibling = otherNode;
						otherNode.leftSibling.rightSibling = otherNode;
					}
					currNode.makeChildOf(otherNode);		//	Make currNode child of otherNode
					pcTable.remove(otherNode.degree);		//	Remove otherNode's entry
					++otherNode.degree;
					if(firstNode == currNode)				
						firstNode = otherNode;				//	Update firstNode pointer
					currNode = otherNode;					//	As currNode is not child of otherNode
				}
			}
			else {
				pcTable.put(currNode.degree, currNode);		//	Add an entry for currNode's degree in the hashtable
				if(currNode == firstNode){					//	Check if we have reached the start of the traversal
					updateMaxNode(firstNode);				//	Reset the maxNode pointer to new MaxNode
					return;									//	Terminate pairwise combine
				}
				currNode = currNode.rightSibling;			// Continue
			}
		}
	}
	
	public void increaseKey(Node thisNode, int amt) {
		Node tempParent = null;
		if(thisNode.parent == null){						//	If thisNode is a top-level(root) node
			thisNode.data += amt;
			updateMaxNode(thisNode);
			return;
		}
		if(thisNode.data + amt <= thisNode.parent.data)		//	If thisNode is non-root node, but still satisfies maxHeap property
			thisNode.data += amt;
		else {								// thisNode would violate maxHeap property. Perform cascading cut.
			thisNode.data += amt;
			tempParent = thisNode.parent;
			cut(thisNode);
			if(tempParent.parent != null && tempParent.childCut == false){	//	If thisNode's parent is not root and has childCut false
				tempParent.childCut = true;
				return;						//	 Stop cascading cut operation
			}
			thisNode = tempParent;			// Shift up one level
			while(thisNode.parent != null && thisNode.childCut == true){	//	Perform while non-root parent's childCut is true.
				tempParent = thisNode.parent;	//	Shift one level up
				cut(thisNode);				//	Cut the node
				thisNode = tempParent;
			}
			if(thisNode.parent != null)		//	Set last non-root node's childCut to true
				thisNode.childCut = true;
		}
	}
	
	private void cut(Node thisNode){
		if(thisNode.rightSibling != thisNode){		//	If thisNode has 1+ sibling
			thisNode.rightSibling.leftSibling = thisNode.leftSibling;	//	Connect the siblings
			thisNode.leftSibling.rightSibling = thisNode.rightSibling;
			if(thisNode.parent.child == thisNode)	//	If thisNode was the first child, update the child pointer of parent
				thisNode.parent.child = thisNode.rightSibling;
		}
		else	//	thisNode has no sibling
			thisNode.parent.child = null;
		--thisNode.parent.degree;
		thisNode.parent = null;
		thisNode.childCut = false;
		insert(thisNode);		//	Insert thisNode in top-level list
	}
}
