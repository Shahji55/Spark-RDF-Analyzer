// ########################## RDF Browser Graphical ##########################
var s; // Global variable for the sigma graph instance.

function arrangeNodesCircular(centralNode, centralNodeURI, neighbors) {
	arrangeNodes(centralNode, centralNodeURI, neighbors, true, calculatePositionCircular);
}

function arrangeNodesByDirection(centralNode, centralNodeURI, neighbors) {
	arrangeNodes(centralNode, centralNodeURI, neighbors, true, calculatePositionByDirection);
}

function arrangeNodesRandomized(centralNode, centralNodeURI, neighbors) {
	arrangeNodes(centralNode, centralNodeURI, neighbors, false, calculatePositionRandomly);
}

function calculatePositionCircular(currEdgeNum, totalNumNeighbors, direction) {
	return { 
		x: Math.cos(Math.PI * 2 * currEdgeNum / totalNumNeighbors),
		y: Math.sin(Math.PI * 2 * currEdgeNum / totalNumNeighbors) 
	};
}

function calculatePositionByDirection(currEdgeNum, totalNumNeighbors, direction) {
	return {
		x: ( direction == 'out' ? 2 : -2 ) * Math.abs(Math.cos(Math.PI * 2 * currEdgeNum / totalNumNeighbors)),
		y: Math.sin(Math.PI * 2 * currEdgeNum / totalNumNeighbors)
	};
}

function calculatePositionRandomly(currEdgeNum, totalNumNeighbors, direction) {
	const quadrant = ( currEdgeNum % 4 ) + 1;
	const xFactor = ( quadrant == 1 || quadrant == 2 ) ? 1 : -1;
	const yFactor = ( quadrant == 1 || quadrant == 4 ) ? 1 : -1;
	const container = {
		width: $('#container').width() / 2,
		height: $('#container').height() / 2
	};

	return {
		x: xFactor * Math.random() * container.width,
		y: yFactor * Math.random() * container.height
	};
}

function arrangeNodes(centralNode, centralNodeURI, neighbors, withEdges, calculatePosition) {
	var nodeCount = 0, edgeCount = 0;
	var numNeighbors = Object.keys(neighbors).length;
	var centralNodeID = 'CENTRALNODE'; // centralNodeURI.slice(1, -1);
	var g = {
		nodes: [],
		edges: []
    };

	// Add central node to the graph instance.
    g.nodes.push({
		id: centralNodeID,
		label: centralNode,
		x: 0,
		y: 0,
		size: 1,
		level: 3,
		labelAlignment: 'bottom',
		type: 'circle',
		direction: 'central'
	});

	// Add all neighbor nodes to the graph instance.
	$.each(neighbors, function(URI, props) {
		// [Log] URI=<http://dbpedia.org/resource/Harry_and_the_Potters>
		// [Log] {predicate: "artist", predicateURI: "<http://dbpedia.org/property/artist>", name: "Harry_and_the_Potters", URI: "<http://dbpedia.org/resource/Harry_and_the_Potters>", direction: "out"}
		
		// Calculate position for node with given function.
		var position = calculatePosition(edgeCount, numNeighbors, props.direction);
		
		// Create default node and edge. Assume OUT-going connection.
		var node = {
			id: 'NEIGHBOR_' + nodeCount,
			uri: URI,
			name: props.name,
			label: props.name,
			type: 'square',
			x: position.x,
			y: position.y,
			size: 0.3,
			level: 1,
			labelAlignment: 'top',
			direction: props.direction
		};

		var edge = {
			id: props.direction + '_e_' + edgeCount,
			label: props.predicate,
			source: centralNodeID,
			target: node.id,
			size: 1
		};

		if( props.direction == 'in' ) {
			// Change properties for IN-going connection.
			edge.source = node.id;
			edge.target = centralNodeID;
		} else if (props.name == '') {
			// Special handling for literals. They don't have a name, but only an URI.
			node.id = 'LITERAL_' + edgeCount;
			node.uri = '';
			node.label = URI.slice(1, -1);
			node.name = URI.slice(1, -1);
			node.type = 'star';
			node.direction = 'literal';

			edge.target = node.id;
		}

		g.nodes.push(node);

		if (withEdges) {
			g.edges.push(edge);
		}

		++nodeCount
		++edgeCount;
	});

	instantiateGraph(g);
	bindListeners();
	// performNOverlap(5);
	designGraph();
}

function instantiateGraph(g) {
 	// Instantiate the sigma instance with the graph data.
	s = new sigma({
		graph: g,
		renderer: {
			container: document.getElementById('container'),
			type: 'canvas'
		},
		settings: SIGMA_GRAPH_SETTINGS
	});
}

function performNOverlap(margin) {
	s.configNoverlap({
		nodeMargin: margin,
		scaleNodes: 1.2,
		easing: 'quadraticInOut', // animation transition function (see sigma.utils.easing for available transitions)
		duration: 1000 // animation duration 
	});
	s.startNoverlap();
}

function bindListeners() {
	s.bind('overNode', function(e) {
		// TODO
		// console.log(e.type, e.data.node, e.data.captor);
	});

	s.bind('outNode', function(e) {
		// TODO
		// console.log(e.type, e.data.node, e.data.captor);
	});

	s.bind('clickNode', function(e) {
		// TODO
		// console.log(e.type, e.data.node, e.data.captor);
	});

	s.bind('doubleClickNode', function(e) {
		// Only browse when clicking a neighbor. Not on central node or a literal.
		if ( (e.data.node.id).startsWith('NEIGHBOR') ) {
			prepareBrowser(e.data.node.name, e.data.node.uri);
		}
	});

	s.bind('overEdge', function(e) {
		// TODO
		// console.log(e.type, e.data.edge, e.data.captor);
	});

	s.bind('outEdge', function(e) {
		// TODO
		// console.log(e.type, e.data.edge, e.data.captor);
	});

	s.bind('clickEdge', function(e) {
		// TODO
		// console.log(e.type, e.data.edge, e.data.captor);
	});
}

function designGraph() {
	var design = sigma.plugins.design(s, {
		styles: {
			nodes: {
				label: {
					by: 'label',
					format: function(value) { 
						return value.substr(0, 10);
					}
				},
				color: {
					by: 'direction',
					scheme: getColorScheme()
				}
			},
			edges: {
				// TODO
			}
		},
		palette: COLORS
	});

	design.apply();
}

function exportGraphAsSVG() {
	var output = s.toSVG({
		download: true,
		filename: 'graphExport.svg',
		size: 1000,
		labels: true,
		data: true
	});
}