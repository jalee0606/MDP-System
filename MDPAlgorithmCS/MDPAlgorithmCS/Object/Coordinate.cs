using System;
namespace MDPAlgorithmCS.Object
{
    public class Coordinate
    {
        public int X { get; set; }
        public int Y { get; set; }

        public Coordinate()
        {

        }

        public Coordinate(int col, int row)
        {
            X = col;
            Y = row;
        }

        /// <summary>
        /// Check if the next coordinate is reachable (Move Left and Move Right only)
        /// </summary>
        /// <param name="c"></param>
        /// <returns></returns>
        public bool Reachable(Coordinate c)
        {
            return
                ((this.X == c.X) && ((c.Y == this.Y + 1) || (c.Y == this.Y - 1)))||((this.Y == c.Y)&&((c.X == this.X + 1)||(c.X == this.X - 1)));
        }
    }
}
