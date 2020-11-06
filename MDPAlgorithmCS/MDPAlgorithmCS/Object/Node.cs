using System;
namespace MDPAlgorithmCS.Object
{
    public class Node
    {
        public bool Confirmed { get; set; }
        public Coordinate Coordinate { get; set; }
        public NodeType Type { get; set; }
        public int ReadAtBlock { get; set; }
    }

    public enum NodeType
    {
        OBSTACLE = 1, EXPLORED = 0, UNEXPLORED = 8
    }
}
