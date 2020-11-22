using System;
using System.IO;

namespace MDPAlgorithmCS.Network
{
    public class Packet
    {
        public readonly byte[] Buffer;
        public readonly byte Type;
        public readonly SerializeReader Reader;

        public readonly SerializeWriter Writer;

        public Packet(byte _type)
        {
            Writer = new SerializeWriter(new MemoryStream());
            Type = _type;

            Writer.Write(id);
        }

        public Packet(GameClient sender, ushort id, byte[] buffer)
        {
            Sender = sender;
            Buffer = buffer;
            Id = id;
            Reader = new SerializeReader(new MemoryStream(Buffer));
        }

    }
}
