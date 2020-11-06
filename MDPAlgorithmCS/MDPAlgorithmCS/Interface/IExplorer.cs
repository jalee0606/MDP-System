using System;
namespace MDPAlgorithmCS.Interface
{
    public interface IExplorer
    {
        void Setup();
        void Run();
        void StartExploring();
    }
}
