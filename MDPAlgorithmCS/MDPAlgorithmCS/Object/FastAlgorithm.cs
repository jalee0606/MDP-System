using System;
using MDPAlgorithmCS.Interface;

namespace MDPAlgorithmCS.Object
{
    public class FastAlgorithm : IAlgorithm
    {
        private Map _algoMap = new Map();
        private Map _presetMap = new Map();
        private Robot _robot = new Robot();

        public FastAlgorithm()
        {
            _algoMap.Initialize();
            _presetMap.Initialize();

        }

        public string getMdfString()
        {
            throw new NotImplementedException();
        }

        public string readSensorReading(double[] sensor)
        {
            throw new NotImplementedException();
        }
    }
}
