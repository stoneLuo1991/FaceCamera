package android.luo.stone.facealigncamera;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    /********************************************************************************
     * ***********************主活动只要涉及的是拍照功能和打开相册功能***********************
     ******************************************************************************/
    private Button takePhoto, choosePhoto;
    private static final int TAKE_PHOTO = 1;
    private static final int CROP_PHOTO = 2;
    private static final int CHOOSE_PHOTO = 3;
    private Uri imageUri;
    private ImageView picture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    /********************************************************************************
     * ***********************c*******创建并显示菜单************************************
     ******************************************************************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();//获取菜单的id

        if (id == R.id.take_photo) {
            File outputImage = new File(Environment.getExternalStorageDirectory(), "output_image.jpg");
            try {
                //如果根目录已经有了相片则删除，并创建新的File保存图片文件
                if (outputImage.exists()) {
                    outputImage.delete();
                }
                outputImage.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //设置启动活动的信使intent（想要启动拍照）
            Intent it = new Intent("android.media.action.IMAGE_CAPTURE");
            //从File文件读取图片你的Uri地址
            imageUri = Uri.fromFile(outputImage);
            //把地址赋值给拍摄到的图片
            it.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            //启动信使，调用拍照功能 TAKE_PHOTO作为启动的标记
            startActivityForResult(it,TAKE_PHOTO);
        }
        else if (id == R.id.choose_photo){
            Intent intent = new Intent(Intent.ACTION_PICK, Uri.parse("content://media/internal/images/media"));
            startActivityForResult(intent, CHOOSE_PHOTO);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    //创建一个剪切图片的信使，并用他调用活动2
                    Intent second = new Intent("com.android.camera.action.CROP");
                    second.setDataAndType(imageUri, "image/*");
                    //向剪切图片中夹带数据（实际主要夹带的地址）
                    second.putExtra("scale", true);
                    second.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(second,CROP_PHOTO);
                    /********************************************************************************
                     * ***********************c*******调用识别系统************************************
                     ******************************************************************************//********************************************************************************
                     * ***********************c*******创建并显示菜单************************************
                     ******************************************************************************//********************************************************************************
                     * ***********************c*******创建并显示菜单************************************
                     ******************************************************************************/
                }
                break;
            case CROP_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        //解析图像的地址以方便显示
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        picture = (ImageView) findViewById(R.id.show_image);
                        picture.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    //获取intent中data（图像数据）的Uri（通用资源标识）
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    //Cursor类保存图像的绝对路径
                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    cursor.close();
                    //加速装载图像
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2;

                    Bitmap temp = BitmapFactory.decodeFile(picturePath, options);
                    //获取图像方向信息
                    int orientation = 0;
                    try {
                        ExifInterface imgParams = new ExifInterface(picturePath);
                        orientation = imgParams.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //图像旋转过程
                    Matrix rotate90 = new Matrix();
                    rotate90.postRotate(orientation);
                    Bitmap originalBitmap = rotateBitmap(temp, orientation);
                    picture = (ImageView) findViewById(R.id.show_image);
                    picture.setImageBitmap(originalBitmap);
                }
                }
        }
    //图像旋转函数
    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }

    }

}
     /*   //调用摄像头拍照
        //关联xml文件的按钮
        takePhoto = (Button) findViewById(R.id.take_photo);
        choosePhoto = (Button) findViewById(R.id.choose_photo);
        //为按钮设置触发事件
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //创建File对象，用于存储拍照的图片,保存在SD卡的根目录上，名字为output_image.jpg
                File outputImage = new File(Environment.getExternalStorageDirectory(), "output_image.jpg");
                try {
                    //如果根目录已经有了相片则删除，并创建新的File保存图片文件
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //设置启动活动的信使intent（想要启动拍照）
                Intent it = new Intent("android.media.action.IMAGE_CAPTURE");
                //从File文件读取图片你的Uri地址
                imageUri = Uri.fromFile(outputImage);
                //把地址赋值给拍摄到的图片
                it.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                //启动信使，调用拍照功能 TAKE_PHOTO作为启动的标记
                startActivityForResult(it,TAKE_PHOTO);
            }
        });
    }



}
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    //创建一个剪切图片的信使，并用他调用活动2
                    Intent second = new Intent("com.android.camera.action.CROP");
                    second.setDataAndType(imageUri, "image/*");
                    //向剪切图片中夹带数据（实际主要夹带的地址）
                    second.putExtra("scale", true);
                    second.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivity();
                }
        }
    }*/