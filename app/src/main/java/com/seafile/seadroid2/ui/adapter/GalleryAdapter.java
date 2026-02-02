package com.seafile.seadroid2.ui.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.viewpager.widget.PagerAdapter;

import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeadroidApplication;
import com.seafile.seadroid2.account.Account;
import com.seafile.seadroid2.data.DataManager;
import com.seafile.seadroid2.data.SeafPhoto;
import com.seafile.seadroid2.transfer.DownloadStateListener;
import com.seafile.seadroid2.transfer.DownloadTask;
import com.seafile.seadroid2.ui.activity.BrowserActivity;
import com.seafile.seadroid2.ui.dialog.GalleryDialog;
import com.seafile.seadroid2.util.ConcurrentAsyncTask;
import com.seafile.seadroid2.util.GlideApp;
import com.seafile.seadroid2.util.Utils;

import java.io.File;
import java.util.List;

import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;

/**
 * Gallery Adapter
 */
public class GalleryAdapter extends PagerAdapter {
    public static final String DEBUG_TAG = "GalleryAdapter";

    /** unique task id */
    public static int taskID;

    private BrowserActivity mActivity;
    private GalleryDialog mFragment;
    private List<SeafPhoto> seafPhotos;
    private LayoutInflater inflater;
    private Account mAccount;
    private DataManager dm;
    private DisplayMetrics displayMetrics;

    public GalleryAdapter(BrowserActivity activity, GalleryDialog fragment, Account account,
                          List<SeafPhoto> photos, DataManager dataManager) {
        SeadroidApplication.getInstance().saveGalleryPhotos(photos);
        mActivity = activity;
        mFragment = fragment;
        seafPhotos = photos;
        inflater = activity.getLayoutInflater();
        mAccount = account;
        dm = dataManager;
        displayMetrics = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    }

    @Override
    public int getCount() {
        return seafPhotos.size();
    }

    public void setItems(List<SeafPhoto> photos) {
        SeadroidApplication.getInstance().saveGalleryPhotos(photos);
        seafPhotos = photos;
    }

    public void downloadPhoto() {
        notifyDataSetChanged();
    }

    @Override
    public View instantiateItem(ViewGroup container, final int position) {
        View contentView = inflater.inflate(R.layout.gallery_view_item, container, false);
        final PhotoView photoView = (PhotoView) contentView.findViewById(R.id.gallery_photoview);
        final ProgressBar progressBar = (ProgressBar) contentView.findViewById(R.id.gallery_progress_bar);
        final String repoName = seafPhotos.get(position).getRepoName();
        final String repoID = seafPhotos.get(position).getRepoID();
        SeafPhoto seafPhoto = seafPhotos.get(position);
        final String filePath = Utils.pathJoin(seafPhoto.getDirPath(),
                seafPhoto.getName());
        final File file = dm.getLocalRepoFile(repoName, repoID, filePath);
        if (file.exists()) {
            Bitmap bitmap = decodeBitmapFromFile(file.getAbsolutePath(), 1024, 1024);
            photoView.setImageBitmap(bitmap);
//            photoView.setImageDrawable(Drawable.createFromPath(file.getAbsolutePath()));
//            GlideApp.with(mActivity).load(file).override(1024, 1024).into(photoView);
            seafPhoto.setDownloaded(true);
        } else {
            progressBar.setVisibility(View.VISIBLE);
            ConcurrentAsyncTask.execute(new DownloadTask(++taskID, mAccount, repoName, repoID, filePath, true, false, -1, new DownloadStateListener() {
                @Override
                public void onFileDownloadProgress(int taskID) {

                }

                @Override
                public void onFileDownloaded(int taskID) {
                    downloadPhoto();
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onFileDownloadFailed(int taskID) {
                    progressBar.setVisibility(View.GONE);
                }
            }));
//            String urlicon = dm.getThumbnailLink(repoName, repoID, filePath, Utils.getThumbnailWidth());
//            progressBar.setVisibility(View.VISIBLE);
//
//            RequestOptions opt = new RequestOptions()
//                    .skipMemoryCache(true)
//                    .override(displayMetrics.widthPixels, displayMetrics.heightPixels);
//
//            GlideApp.with(mActivity)
//                    .asFile()
//                    .load(GlideLoadConfig.getGlideUrl(urlicon))
//                    .override(1024, 1024)
//                    .apply(opt)
//                    .into(new Target<File>() {
//                              @Override
//                              public void onLoadStarted(@Nullable Drawable placeholder) {}
//                              @Override
//                              public void onLoadFailed(@Nullable Drawable errorDrawable) {
//                                  progressBar.setVisibility(View.GONE);
//                              }
//                              @Override
//                              public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
//                                  progressBar.setVisibility(View.GONE);
//                                  photoView.setImageBitmap(Utils.getBitmapFromFile(resource));
//                              }
//                              @Override
//                              public void onLoadCleared(@Nullable Drawable placeholder) {}
//                              @Override
//                              public void getSize(@NonNull SizeReadyCallback cb) {}
//                              @Override
//                              public void removeCallback(@NonNull SizeReadyCallback cb) {}
//                              @Override
//                              public void setRequest(@Nullable Request request) {}
//                              @Nullable
//                              @Override
//                              public Request getRequest() { return null; }
//                              @Override
//                              public void onStart() {}
//                              @Override
//                              public void onStop() {}
//                              @Override
//                              public void onDestroy() {}
//                          }
//                    );
//            GlideApp.with(mActivity)
//                    .asBitmap()
//                    .load(glideUrl)
//                    .override(1024, 1024)
//                    .apply(opt)
//                    .listener(new RequestListener<Bitmap>() {
//                        @Override
//                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
//                            progressBar.setVisibility(View.GONE);
//                            return false;
//                        }
//
//                        @Override
//                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
//                            progressBar.setVisibility(View.GONE);
//                            return false;
//                        }
//                    })
//                    .into(photoView);
        }

        photoView.setOnPhotoTapListener(new OnPhotoTapListener() {
            @Override
            public void onPhotoTap(ImageView view, float x, float y) {
                mFragment.hideOrShowToolBar();
            }
        });

        container.addView(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        return contentView;
    }

    public Bitmap decodeBitmapFromFile(String path, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public int calculateInSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * when you call notifyDataSetChanged(),
     * the view pager will remove all views and reload them all.
     * As so the reload effect is obtained.
     */
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
//        GlideApp.with(mActivity).clear((View) object);
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
