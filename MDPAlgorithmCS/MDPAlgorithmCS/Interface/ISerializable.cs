using System;
using MDPAlgorithmCS.Network;

namespace MDPAlgorithmCS.Interface
{
    public interface ISerializable
    {
        void Serialize(SerializeWriter writer);
    }
}
