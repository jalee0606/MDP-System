using System;
namespace MDPAlgorithmCS.Object
{
    public class Robot
    {
        public Coordinate RobotCenter { get; set; }

        public Robot()
        {
            RobotCenter = new RobotCoordinate(Direction.North, 1, 1);

        }
    }
}
