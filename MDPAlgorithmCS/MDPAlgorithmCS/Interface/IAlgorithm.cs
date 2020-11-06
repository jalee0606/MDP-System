using System;
namespace MDPAlgorithmCS.Interface
{
    public interface IAlgorithm
    {
        string readSensorReading(double[] sensor);
        string getMdfString();
    }
}
