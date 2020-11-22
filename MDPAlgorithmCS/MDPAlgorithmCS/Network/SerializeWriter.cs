using System.IO;

namespace MDPAlgorithmCS.Network
{
    public class SerializeWriter : BinaryWriter
    {
        private readonly Encoding _encoding = Encoding.Unicode;

        public SerializeWriter(Stream stream) : base(stream, Encoding.Unicode)
        {
        }

        public SerializeWriter(Stream stream, Encoding encoding) : base(stream, encoding)
        {
            _encoding = encoding;
        }

        public byte[] GetBuffer()
        {
            return (BaseStream as MemoryStream)?.ToArray();
        }

        public void Write(ISerializable model)
        {
            model.Serialize(this);
        }

        public void Write(Vector3 vec3)
        {
            Write(vec3.X);
            Write(vec3.Y);
            Write(vec3.Z);
        }

        /// <summary>
        /// Write a string to the underlying buffer with the default encoding which is Unicode
        /// </summary>
        /// <param name="text">the text to be send over the stream</param>
        /// <param name="prefixLength">to indicate if the packet contain the str length for processing</param>
        public void WriteText(string text, bool prefixLength = true)
        {
            if (text == null)
                text = "";

            text += "\0";

            var buf = _encoding.GetBytes(text);

            if (prefixLength)
                Write((ushort)text.Length);

            Write(buf);
        }

        /// <summary>
        /// Write a string with a prefixed max length to the underlying buffer with the default encoding which is Unicode
        /// </summary>
        /// <param name="str"></param>
        /// <param name="maxLength"></param>
        /// <param name="nullTerminated"></param>
        public void WriteTextStatic(string str, int maxLength, bool nullTerminated = false)
        {
            if (str == null)
                str = "";

            if (str.Length > maxLength)
                str.Substring(0, maxLength);

            if (nullTerminated)
            {
                if (str.Length > maxLength - 1)
                    str = str.Substring(0, maxLength - 1);
                str += "\0";
            }

            var tempBuf = _encoding.GetBytes(str);

            var buf = new byte[maxLength * 2];
            Array.Copy(tempBuf, 0, buf, 0, tempBuf.Length);

            Write(buf);

        }

        public static string HexDump(IEnumerable<byte> buffer)
        {
            const int bytesPerLine = 16;
            var hexDump = "";
            var j = 0;
            foreach (var g in buffer.Select((c, i) => new { Char = c, Chunk = i / bytesPerLine }).GroupBy(c => c.Chunk))
            {
                var s1 = g.Select(c => $"{c.Char:X2} ").Aggregate((s, i) => s + i);
                string s2 = null;
                var first = true;
                foreach (var c in g)
                {
                    var s = $"{(c.Char < 32 || c.Char > 122 ? '·' : (char)c.Char)} ";
                    if (first)
                    {
                        first = false;
                        s2 = s;
                        continue;
                    }
                    s2 = s2 + s;
                }
                var s3 = $"{j++ * bytesPerLine:d6}: {s1} {s2}";
                hexDump = hexDump + s3 + Environment.NewLine;
            }
            return hexDump;
        }
    }
}