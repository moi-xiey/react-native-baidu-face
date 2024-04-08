/**
 * Copyright (C) 2017 Baidu Inc. All rights reserved.
 */
package com.baidu.idl.face.platform.decode;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Base64;
import android.util.Log;

import com.baidu.idl.face.platform.FaceStatusEnum;
import com.baidu.idl.face.platform.IDetect;
import com.baidu.idl.face.platform.ILiveness;
import com.baidu.idl.face.platform.LivenessTypeEnum;
import com.baidu.idl.face.platform.model.FaceExtInfo;
import com.baidu.idl.face.platform.model.FaceModel;
import com.baidu.idl.face.platform.utils.BitmapUtils;
import com.baidu.idl.facesdk.FaceInfo;
import com.baidu.idl.facesdk.FaceSDK;
import com.baidu.idl.facesdk.FaceTracker;
import com.baidu.idl.facesdk.FaceVerifyData;

import java.util.ArrayList;
import java.util.Date;

/**
 * 人脸跟踪,活体检测功能类
 */
public class FaceModule implements IDetect, ILiveness {

    private static final String TAG = FaceModule.class.getSimpleName();
    // 人脸检测功能接口
    private FaceTracker mFaceTracker;
    // 图像宽度，高度
    private int mImageWidth = 0;
    private int mImageHeight = 0;
    // 检测图像
    private int[] mArgbData = null;
    // 检测图像
    private int[] mSaveFaceArgbData = null;
    // 检测状态
    private int mErrCode = 0;
    // 活体检测
    private boolean mLivenessFlag = false;

    private FaceModel mFaceModel;
    private FaceExtInfo[] mFaceExtInfos;
    private FaceExtInfo mFaceExtInfo;

    private int mDegree = 270;

    public FaceModule(FaceTracker tracker) {
        mFaceTracker = tracker;
        if (mFaceTracker != null) {
            mFaceTracker.clearTrackedFaces();
            Log.e(TAG, "clear tracker");
        }
    }

    public FaceModel detect(byte[] imageData, int imageWidth, int imageHeight) {
        FaceModel model = null;
        if (imageData != null && imageWidth > 0 && imageHeight > 0) {

            Log.e(TAG, "detect--------imageWidth:"+ imageWidth+ ", imageHeight:" + imageHeight);
//            if (mLivenessFlag) {
//                mFaceTracker.set_isVerifyLive(false);
//                mLivenessFlag = false;
//            }


//            if (mFaceModel == null) {
            model = new FaceModel();
//            }
            FaceInfo[] faceInfos = faceTrackerDecode(imageData, imageWidth, imageHeight);
            model.setArgbImage(mArgbData);
            model.setFaceInfos(getExtInfo(faceInfos));
            model.setFaceModuleState(getModuleState(mErrCode));
            model.setFrameTime(System.currentTimeMillis());
        }
        return model;
    }

    public FaceModel liveness(LivenessTypeEnum type, byte[] imageData, int imageWidth, int imageHeight) {
        FaceModel model = null;
        if (imageData != null && type != null && imageWidth > 0 && imageHeight > 0) {
            Log.e(TAG, "liveness--------");

//            if (!mLivenessFlag) {
//                mFaceTracker.set_isVerifyLive(true);
//                mLivenessFlag = true;
//            }

//            if (mFaceModel == null) {
            model = new FaceModel();
//            }

            FaceInfo[] faceInfos = faceTrackerDecode(imageData, imageWidth, imageHeight);
            model.setArgbImage(mArgbData);
            model.setFaceInfos(getExtInfo(faceInfos));
            model.setFaceModuleState(getModuleState(mErrCode));
            model.setFrameTime(System.currentTimeMillis());
        }
        return model;
    }

    private FaceExtInfo[] getExtInfo(FaceInfo[] faceInfos) {
//        FaceExtInfo[] faceExtInfos = null;
//        if (faceInfos != null && faceInfos.length > 0) {
//            faceExtInfos = new FaceExtInfo[faceInfos.length];
//            for (int i = 0; i < faceInfos.length; i++) {
//                faceExtInfos[i] = new FaceExtInfo(faceInfos[i]);
//            }
//        }
//        return faceExtInfos;

        if (mFaceExtInfos == null) {
            mFaceExtInfos = new FaceExtInfo[1];
            mFaceExtInfo = new FaceExtInfo();
        }

        if (faceInfos != null && faceInfos.length > 0) {
            if (mFaceExtInfo == null) {
                mFaceExtInfo = new FaceExtInfo();
            }
            mFaceExtInfo.addFaceInfo(faceInfos[0]);
            mFaceExtInfos[0] = mFaceExtInfo;
        } else {
            mFaceExtInfos[0] = null;
        }

        return mFaceExtInfos;
    }

    private FaceStatusEnum getModuleState(int errCode) {
        FaceStatusEnum status = FaceStatusEnum.Detect_NoFace;

        if (errCode == FaceTracker.ErrCode.OK.ordinal()) {
            status = FaceStatusEnum.OK;
        } else if (errCode == FaceTracker.ErrCode.PITCH_OUT_OF_DOWN_MAX_RANGE.ordinal()) {
            status = FaceStatusEnum.Detect_PitchOutOfDownMaxRange;
        } else if (errCode == FaceTracker.ErrCode.PITCH_OUT_OF_UP_MAX_RANGE.ordinal()) {
            status = FaceStatusEnum.Detect_PitchOutOfUpMaxRange;
        } else if (errCode == FaceTracker.ErrCode.YAW_OUT_OF_LEFT_MAX_RANGE.ordinal()) {
            status = FaceStatusEnum.Detect_PitchOutOfLeftMaxRange;
        } else if (errCode == FaceTracker.ErrCode.YAW_OUT_OF_RIGHT_MAX_RANGE.ordinal()) {
            status = FaceStatusEnum.Detect_PitchOutOfRightMaxRange;
        } else if (errCode == FaceTracker.ErrCode.POOR_ILLUMINATION.ordinal()) {
            status = FaceStatusEnum.Detect_PoorIllumintion;
        } else if (errCode == FaceTracker.ErrCode.NO_FACE_DETECTED.ordinal()) {
            status = FaceStatusEnum.Detect_NoFace;
        } else if (errCode == FaceTracker.ErrCode.DATA_NOT_READY.ordinal()) {
            status = FaceStatusEnum.Detect_DataNotReady;
        } else if (errCode == FaceTracker.ErrCode.DATA_HIT_ONE.ordinal()) {
            status = FaceStatusEnum.Detect_DataHitOne;
        } else if (errCode == FaceTracker.ErrCode.DATA_HIT_LAST.ordinal()) {
            status = FaceStatusEnum.Detect_DataHitLast;
        } else if (errCode == FaceTracker.ErrCode.IMG_BLURED.ordinal()) {
            status = FaceStatusEnum.Detect_ImageBlured;
        } else if (errCode == FaceTracker.ErrCode.OCCLUSION_LEFT_EYE.ordinal()) {
            status = FaceStatusEnum.Detect_OccLeftEye;
        } else if (errCode == FaceTracker.ErrCode.OCCLUSION_RIGHT_EYE.ordinal()) {
            status = FaceStatusEnum.Detect_OccRightEye;
        } else if (errCode == FaceTracker.ErrCode.OCCLUSION_NOSE.ordinal()) {
            status = FaceStatusEnum.Detect_OccNose;
        } else if (errCode == FaceTracker.ErrCode.OCCLUSION_MOUTH.ordinal()) {
            status = FaceStatusEnum.Detect_OccMouth;
        } else if (errCode == FaceTracker.ErrCode.OCCLUSION_LEFT_CONTOUR.ordinal()) {
            status = FaceStatusEnum.Detect_OccLeftContour;
        } else if (errCode == FaceTracker.ErrCode.OCCLUSION_RIGHT_CONTOUR.ordinal()) {
            status = FaceStatusEnum.Detect_OccRightContour;
        } else if (errCode == FaceTracker.ErrCode.OCCLUSION_CHIN_CONTOUR.ordinal()) {
            status = FaceStatusEnum.Detect_OccChin;
        } else if (errCode == FaceTracker.ErrCode.FACE_NOT_COMPLETE.ordinal()) {
            status = FaceStatusEnum.Detect_FaceNotComplete;
        } else if (errCode == FaceTracker.ErrCode.UNKNOW_TYPE.ordinal()) {
            status = FaceStatusEnum.Detect_NoFace;
        }
//        Log.e(TAG, "decode status " + status.name());
        return status;
    }

    @Override
    public int[] getBestFaceImage() {
        return mSaveFaceArgbData;
    }

    public void setPreviewDegree(int degree) {
        Log.e(TAG, "-----setPreviewDegree-----degree:"+ degree);
//        if (degree >= 0 && degree <= 360) {
//            mDegree = degree;
//        }
    }

    public String getDetectBestImage(int faceId) {
        Log.e(TAG, "getDetectBestImage--------");
        String imageEncode = "";

        FaceVerifyData[] faceVerifyDatas = mFaceTracker.get_FaceVerifyData(0);
        if (faceVerifyDatas != null && faceVerifyDatas.length > 0) {
            int index = faceVerifyDatas.length - 1;
            Bitmap image = Bitmap.createBitmap(
                    faceVerifyDatas[index].cols, faceVerifyDatas[index].rows,
                    Bitmap.Config.ARGB_8888);
            image.setPixels(faceVerifyDatas[index].mRegImg, 0, faceVerifyDatas[index].cols, 0, 0,
                    faceVerifyDatas[index].cols, faceVerifyDatas[index].rows);
            imageEncode = BitmapUtils.bitmapToJpegBase64(image, 70);
            if (imageEncode != null && imageEncode.length() > 0) {
                imageEncode = imageEncode.replace("\\/", "/");
            }

//            Log.e(TAG, mFaceTracker.get_FaceVerifyData(1).length+"");
//            if(imageEncode.length() > 4000) {
//                for(int i=0;i<imageEncode.length();i+=4000){
//                    if(i+4000<imageEncode.length())
//                        Log.i(TAG,imageEncode.substring(i, i+4000));
//                    else
//                        Log.i(TAG,imageEncode.substring(i, imageEncode.length()));
//                }
//            } else {
//                Log.i(TAG, imageEncode);
//            }
        }
        return imageEncode;
    }


    public ArrayList<String> getDetectBestImageList() {
        Log.e(TAG, "getDetectBestImageList--------mImageWidth:"+mImageWidth+", mImageHeight"+mImageHeight);
        ArrayList<String> list = new ArrayList<String>();
        //FaceVerifyData[] faceVerifyDatas = mFaceTracker.get_FaceVerifyData(0);
//        if (faceVerifyDatas != null && faceVerifyDatas.length > 0) {
            String imageEncode = "";
            // for (int i = 0; i < faceVerifyDatas.length; i++) {
                Bitmap image = Bitmap.createBitmap(getBestFaceImage(), 0, mImageWidth, mImageWidth, mImageHeight, Bitmap.Config.ARGB_8888);
                //image.eraseColor(Color.WHITE);

//                image.setPixels(mSaveFaceArgbData, 0, mImageWidth, 0, 0,
//                        mImageWidth, mImageHeight);
                imageEncode = BitmapUtils.bitmapToJpegBase64(image, 70);

                if (imageEncode != null && imageEncode.length() > 0) {
                    imageEncode = imageEncode.replace("\\/", "/");
                }
                list.add(imageEncode);

//            }
            if(imageEncode.length() > 4000) {
                for(int i=0;i<imageEncode.length();i+=4000){
                    if(i+4000<imageEncode.length())
                        Log.i(TAG,imageEncode.substring(i, i+4000));
                    else
                        Log.i(TAG,imageEncode.substring(i, imageEncode.length()));
                }
            } else {
                Log.i(TAG, imageEncode);
            }

//        }
        return list;
    }

    private FaceInfo[] faceTrackerDecode(byte[] imageData, int imageWidth, int imageHeight) {
        FaceInfo[] faces = null;

        if (mArgbData == null ||
                (imageWidth * imageHeight) != (mImageWidth * mImageHeight)) {
            mArgbData = new int[imageWidth * imageHeight];
            mImageWidth = imageWidth;
            mImageHeight = imageHeight;
        }

        long startTime = System.nanoTime();
        if (FaceSDK.getAuthorityStatus() == 0) {


            int argb = FaceSDK.getARGBFromYUVimg(imageData, mArgbData,
                    imageWidth, imageHeight,
                    mDegree, 0);
            if (mDegree %270 ==0) {
                int temp = imageWidth;
                mImageWidth = imageHeight;
                mImageHeight = temp;
            }
            Log.e(TAG, "----argb----"+ argb);
            // rows=768-cols=432
            FaceTracker.ErrCode errorCode = mFaceTracker.faceVerification(
                    mArgbData, imageWidth, imageHeight,
                    FaceSDK.ImgType.ARGB,
                    FaceTracker.ActionType.RECOGNIZE);
            mErrCode = errorCode.ordinal();
            faces = mFaceTracker.get_TrackedFaceInfo();

            long endingTime = System.nanoTime();
            Float fps = 1000000000.0f / (endingTime - startTime + 1);

            if (faces != null && faces.length > 0) {
//                Log.e(TAG, "face decode fps " + fps + "-" + errorCode.name());
                if (mErrCode == FaceTracker.ErrCode.OK.ordinal()) {
                    mSaveFaceArgbData = mArgbData;

                    long t = new Date().getTime();
                    Log.e(TAG+ t, "----faceTrackerDecode------");

//                    String imageEncode = Base64.encodeToString(imageData, Base64.DEFAULT);
//                    if(imageEncode.length() > 4000) {
//                        for(int i=0;i<imageEncode.length();i+=4000){
//                            if(i+4000<imageEncode.length())
//                            Log.i(TAG+t,imageEncode.substring(i, i+4000));
//                            else
//                            Log.i(TAG+t,imageEncode.substring(i, imageEncode.length()));
//                        }
//                    } else {
//                        Log.i(TAG+t, imageEncode);
//                    }
                }
            } else {
                Log.e(TAG, "face decode " + errorCode.name());
            }
        }
        return faces;
    }

    public void reset() {
        if (mFaceTracker != null) {
            mFaceTracker.re_collect_reg_imgs();
            mFaceTracker.clearTrackedFaces();
        }
    }
}
