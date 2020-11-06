using System;
namespace MDPAlgorithmCS.Object
{
    public class RobotCoordinate : Coordinate
    {
        public Direction Direction { get; set; }

        public RobotCoordinate() : base()
        {
        }

        public RobotCoordinate(Direction d, int col, int row) : base(col, row)
        {
            Direction = d;
        }
    }

    public enum Direction
    {
        North = 'N', South = 'S', West = 'W', East = 'E'
    }
}
