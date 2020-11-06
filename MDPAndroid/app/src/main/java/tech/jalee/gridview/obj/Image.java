package tech.jalee.gridview.obj;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import com.example.testapp4.R;

public class Image extends Position {

    private ImageType _imageType;

    public Image(ImageType type, int xPos, int yPos)
    {
        super(yPos, xPos);
        _imageType = type;
    }

    public Bitmap getBitmap(Context context, int size)
    {
        return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), this._imageType.res()), size-1, size-1, false);
    }

    @Override
    public String toString()
    {
        return "(" + this._imageType.id() + "," + super.getX() + "," + super.getY() + ")";
    }

    public static Image fromString(String jsonStr)
    {
        try {
            String[] vars = jsonStr.split(",");
            int imageId = Integer.valueOf(vars[0].trim());
            if(imageId > 0 && imageId < 16) {
                int xPos = Integer.valueOf(vars[1].trim());
                int yPos = Integer.valueOf(vars[2].trim());
                ImageType _type = ImageType.castFrom(imageId);
                return new Image(_type, xPos, yPos);
            } else {
                return null;
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public enum ImageType {

        UP(1), DOWN(2), LEFT(3), RIGHT(4), GO(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9), ZERO(10), V(11), W(12), X(13), Y(14), Z(15);

        private final int resId;
        private final int id;

        private ImageType(int id)
        {
            this.id = id;
            switch(id)
            {
                case 1:
                    resId = R.drawable.up;
                    break;
                case 2:
                    resId = R.drawable.down;
                    break;
                case 3:
                    resId = R.drawable.right;
                    break;
                case 4:
                    resId = R.drawable.left;
                    break;
                case 5:
                    resId = R.drawable.go;
                    break;
                case 6:
                    resId = R.drawable.six;
                    break;
                case 7:
                    resId = R.drawable.seven;
                    break;
                case 8:
                    resId = R.drawable.eight;
                    break;
                case 9:
                    resId = R.drawable.nine;
                    break;
                case 10:
                    resId = R.drawable.zero;
                    break;
                case 11:
                    resId = R.drawable.alpha_v;
                    break;
                case 12:
                    resId = R.drawable.alpha_w;
                    break;
                case 13:
                    resId = R.drawable.alpha_x;
                    break;
                case 14:
                    resId = R.drawable.alpha_y;
                    break;
                case 15:
                    resId = R.drawable.alpha_z;
                    break;
                default:
                    resId = R.drawable.ic_launcher_foreground;

            }
        }

        public static ImageType castFrom(int id)
        {
            switch(id)
            {
                case 1:
                    return UP;
                case 2:
                    return DOWN;
                case 3:
                    return RIGHT;
                case 4:
                    return LEFT;
                case 5:
                    return GO;
                case 6:
                    return SIX;
                case 7:
                    return SEVEN;
                case 8:
                    return EIGHT;
                case 9:
                    return NINE;
                case 10:
                    return ZERO;
                case 11:
                    return V;
                case 12:
                    return W;
                case 13:
                    return X;
                case 14:
                    return Y;
                case 15:
                    return Z;
                default:
                    return null;
            }
        }

        public int id()
        {
            return this.id;
        }

        public int res()
        {
            return this.resId;
        }
    }

}
