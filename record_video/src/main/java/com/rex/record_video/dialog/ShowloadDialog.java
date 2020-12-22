package com.rex.record_video.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rex.record_video.R;

/**
 * author: rexkell
 * date: 2020/12/15
 * explain:
 */
public class ShowloadDialog extends Dialog {

    public ShowloadDialog(@NonNull Context context) {
        super(context);
    }

    public ShowloadDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
    }

    public static class Builder {
        private Context context;
        private String title;
        private String message;
        private View contentView;
        private int LayoutId;
        private OnClickListener positiveButtonClickListener;
        private OnClickListener negativeButtonClickListener;
        private CompoundButton.OnCheckedChangeListener onCheckedChangeListener;
        private boolean isCanceledOutside;
        private String btnYesText;
        private String btnCancelText;
        private boolean isCancelable=true;

        public Builder(Context context) {
            this.context = context;
        }


        public ShowloadDialog.Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        /**
         * Set the Dialog message from resource
         *
         * @return
         */
        public ShowloadDialog.Builder setMessage(int message) {
            this.message = (String) context.getText(message);
            return this;
        }

        /**
         * Set the Dialog title from resource
         *
         * @param title
         * @return
         */
        public ShowloadDialog.Builder setTitle(int title) {
            this.title = (String) context.getText(title);
            return this;
        }

        /**
         * Set the Dialog title from String
         *
         * @param title
         * @return
         */
        public ShowloadDialog.Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * Set contentView
         *
         * @return
         */
        public ShowloadDialog.Builder setContentView(int layoutId) {
            this.LayoutId = layoutId;
            this.contentView = layoutId == 0 ? null : LayoutInflater.from(context).inflate(layoutId, null);
            return this;
        }

        /**
         * Set the positive button resource and it's listener
         *
         * @return
         */
        public ShowloadDialog.Builder setPositiveButton(OnClickListener listener) {
            this.positiveButtonClickListener = listener;
            return this;
        }

        /**
         * Set the positiveButton textContent
         */
        public ShowloadDialog.Builder setPositiveButtonText(String btnText) {
            this.btnYesText = btnText;
            return this;
        }
        /**
         * Set the checkBox select listener*/
        public ShowloadDialog.Builder setCheckBox(CompoundButton.OnCheckedChangeListener listener){
            this.onCheckedChangeListener=listener;
            return this;
        }


        /**
         * Set the negative button resource and it's listener
         */
        public ShowloadDialog.Builder setNegativeButton(OnClickListener listener) {
            this.negativeButtonClickListener = listener;
            return this;
        }

        /**
         * Set the NagativeButton textContent
         */
        public ShowloadDialog.Builder setNegativeButtonText(String btntext){
            this.btnCancelText=btntext;
            return this;
        }
        public ShowloadDialog.Builder setCanceledOutside(boolean isCanceled) {
            this.isCanceledOutside = isCanceled;
            return this;
        }
        public ShowloadDialog.Builder setCancelable(boolean isCancelable){
            this.isCancelable=isCancelable;
            return this;
        }

        public ShowloadDialog createLoadingDialog() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final ShowloadDialog dialog = new ShowloadDialog(context, R.style.VideoDialogStyle);
            View view = inflater.inflate(R.layout.dialog_video_loading, null);
            dialog.setCanceledOnTouchOutside(isCanceledOutside);
            dialog.addContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//            ((TextView) view.findViewById(R.id.tv_dialog_title)).setText(title);
            TextView tvMessage=(TextView) view.findViewById(R.id.tv_loadingmsg);
            if (TextUtils.isEmpty(message)){
                tvMessage.setVisibility(View.GONE);
            }else {
                tvMessage.setText(message);
                tvMessage.setVisibility(View.VISIBLE);
            }
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.video_anim_progressloading);
            ((ImageView) view.findViewById(R.id.loadingImageView)).startAnimation(animation);

            return dialog;
        }


    }
}
