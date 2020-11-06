using System;
using System.Collections.Generic;
using System.Linq;

namespace MDPAlgorithmCS.Object
{
    public class Map
    {
        public List<Node> Nodes { get; set; }

        public Map()
        {
            Nodes = new List<Node>();
        }

        public void Initialize()
        {
            for(int row = 0; row < Constant.ROW_LENGTH; ++row)
            {
                for(int col = 0; col < Constant.COL_LENGTH; ++col)
                {
                    Node n = new Node();
                    n.Confirmed = false;
                    n.Coordinate = new Coordinate(col, row);
                    n.Type = NodeType.UNEXPLORED;
                    n.ReadAtBlock = Constant.ROW_LENGTH;
                    Nodes.Add(n);
                }
            }
        }

        public void Update(Node n)
        {
            Node fromList = Nodes.Single(x => x.Coordinate.Equals(n.Coordinate) && x.Confirmed == false && x.ReadAtBlock > n.ReadAtBlock);
            if(fromList != null)
            {
                fromList.ReadAtBlock = n.ReadAtBlock;
                fromList.Confirmed = n.Confirmed;
                fromList.Type = n.Type;
            }
        }
    }
}
