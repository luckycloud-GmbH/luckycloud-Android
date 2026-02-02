package com.seafile.seadroid2.ui.fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeafConnection;
import com.seafile.seadroid2.account.Account;
import com.seafile.seadroid2.data.DataManager;
import com.seafile.seadroid2.data.SeafLink;
import com.seafile.seadroid2.ui.activity.ShareDialogActivity;
import com.seafile.seadroid2.ui.adapter.SeafTimeAdapter;
import com.seafile.seadroid2.util.ConcurrentAsyncTask;
import com.seafile.seadroid2.util.Utils;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


public class ShareDownloadFragment extends Fragment {

    public static String LINK_PREVIEW = "Link preview";
    public static String DIRECT_DOWNLOAD = "Direct download";
    public static String EMBEDDED_WEB_LINK = "Embedded web link";

    private DataManager dataManager;

    private View shareCreateLinkLayout;
    private View shareCopyLinkLayout;
    private SwitchCompat passwordProtectionSwitch;
    private LinearLayout passwordProtectionLayout;
    private EditText passwordEdit;
    private ImageView showPasswordImage;
    private EditText passwordConfirmEdit;
    private SwitchCompat autoExpirationSwitch;
    private RadioGroup autoExpirationRadioGroup;
    private RadioButton expirationDaysRB;
    private LinearLayout expirationDaysLayout;
    private EditText expirationDaysEdit;
    private RadioButton expirationTimeRB;
    private View expirationTimeLayout;
    private EditText expirationTimeEdit;
    private CardView expirationTimeCard;
    private View selectPermissionLayout;
    private EditText selectPermissionText;
    private CardView selectPermissionCard;
    private CardView createCard;
    private View linkLayout;
    private EditText linkText;
    private CardView copyCard;
    private CardView selectLinkCard;
    private CardView qrCard;
    private TextView currentPasswordCaptionText;
    private View currentPasswordLayout;
    private TextView currentPasswordText;
    private ImageView showCurrentPasswordImage;
    private TextView currentExpirationCaptionText;
    private View currentExpirationLayout;
    private TextView currentExpirationText;
    private ImageView editExpirationImage;
    private View updateExpirationLayout;
    private RadioGroup updateAutoExpirationRadioGroup;
    private RadioButton updateExpirationDaysRB;
    private View updateExpirationDaysLayout;
    private TextView updateExpirationDaysEdit;
    private RadioButton updateExpirationTimeRB;
    private View updateExpirationTimeLayout;
    private EditText updateExpirationTimeEdit;
    private CardView updateExpirationTimeCard;
    private CardView updateExpirationCard;
    private CardView cancelExpirationCard;
    private View updatePermissionLayout;
    private EditText updatePermissionText;
    private CardView updatePermissionCard;
    private CardView sendCard;
    private CardView deleteCard;
    private View popupSelectDatetimeView;
    private View popupSelectLinkView;
    private View popupSelectPermissionView;
    private View popupSelectPermissionItemView;
    private PopupWindow mLinkDropDown = null;
    private PopupWindow mDateTimeDropDown = null;
    private PopupWindow mPermissionDropDown = null;

    private ShareDialogActivity mShareDialogActivity;
    private Account mAccount;
    private String dialogType;
    private Calendar calendar;
    private String selectedTime;
    private SeafConnection conn;
    private SeafLink seafLink;
    private Account account;
    private String repoID;
    private String path;
    private String fileName;
    private SimpleDateFormat simpleDateFormatForServer;
    private SimpleDateFormat simpleDateFormatForPhone;

    private Boolean isShowNewPassword = false;
    private Boolean isShowCurrentPassword = false;

    private Boolean isDownloadFragment = true;

    private String linkType = LINK_PREVIEW;

    public ShareDownloadFragment(boolean downloadFragment) {
        isDownloadFragment = downloadFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.share_download_fragment, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initViewAction();
        init();
    }

    private void initView(View root) {
        shareCreateLinkLayout = root.findViewById(R.id.share_create_link_layout);
        shareCopyLinkLayout = root.findViewById(R.id.share_copy_link_layout);
        passwordProtectionSwitch = root.findViewById(R.id.password_protection_switch);
        passwordProtectionLayout = root.findViewById(R.id.password_protection_layout);
        passwordEdit = root.findViewById(R.id.password_edit);
        showPasswordImage = root.findViewById(R.id.show_password_image);
        passwordConfirmEdit = root.findViewById(R.id.password_confirm_edit);
        autoExpirationSwitch = root.findViewById(R.id.auto_expiration_switch);
        autoExpirationRadioGroup = root.findViewById(R.id.auto_expiration_radio_group);
        expirationDaysRB = root.findViewById(R.id.expiration_days_rb);
        expirationDaysEdit = root.findViewById(R.id.expiration_days_edit);
        expirationDaysLayout = root.findViewById(R.id.expiration_days_layout);
        expirationTimeRB = root.findViewById(R.id.expiration_time_rb);
        expirationTimeLayout = root.findViewById(R.id.expiration_time_layout);
        expirationTimeEdit = root.findViewById(R.id.expiration_time_edit);
        expirationTimeCard = root.findViewById(R.id.expiration_time_card);
        selectPermissionLayout = root.findViewById(R.id.select_permission_layout);
        selectPermissionText = root.findViewById(R.id.select_permission_text);
        selectPermissionCard = root.findViewById(R.id.select_permission_card);
        createCard = root.findViewById(R.id.create_card);
        linkLayout = root.findViewById(R.id.link_layout);
        linkText = root.findViewById(R.id.link_text);
        copyCard = root.findViewById(R.id.copy_link_card);
        selectLinkCard = root.findViewById(R.id.select_link_card);
        qrCard = root.findViewById(R.id.qr_card);
        currentPasswordCaptionText = root.findViewById(R.id.current_password_caption_text);
        currentPasswordLayout = root.findViewById(R.id.current_password_layout);
        currentPasswordText = root.findViewById(R.id.current_password_text);
        showCurrentPasswordImage = root.findViewById(R.id.show_current_password_image);
        currentExpirationCaptionText = root.findViewById(R.id.current_expiration_caption_text);
        currentExpirationLayout = root.findViewById(R.id.current_expiration_layout);
        currentExpirationText = root.findViewById(R.id.current_expiration_text);
        editExpirationImage = root.findViewById(R.id.edit_expiration_image);
        updateExpirationLayout = root.findViewById(R.id.update_expiration_layout);
        updateAutoExpirationRadioGroup = root.findViewById(R.id.update_auto_expiration_radio_group);
        updateExpirationDaysRB = root.findViewById(R.id.update_expiration_days_rb);
        updateExpirationDaysLayout = root.findViewById(R.id.update_expiration_days_layout);
        updateExpirationDaysEdit = root.findViewById(R.id.update_expiration_days_edit);
        updateExpirationTimeRB = root.findViewById(R.id.update_expiration_time_rb);
        updateExpirationTimeLayout = root.findViewById(R.id.update_expiration_time_layout);
        updateExpirationTimeEdit = root.findViewById(R.id.update_expiration_time_edit);
        updateExpirationTimeCard = root.findViewById(R.id.update_expiration_time_card);
        updateExpirationCard = root.findViewById(R.id.update_expiration_card);
        cancelExpirationCard = root.findViewById(R.id.cancel_expiration_card);
        updatePermissionLayout = root.findViewById(R.id.update_permission_layout);
        updatePermissionText = root.findViewById(R.id.update_permission_text);
        updatePermissionCard = root.findViewById(R.id.update_permission_card);
        sendCard = root.findViewById(R.id.send_card);
        deleteCard = root.findViewById(R.id.delete_card);
        popupSelectDatetimeView = root.findViewById(R.id.popup_select_datetime_layout);
        popupSelectLinkView = root.findViewById(R.id.popup_select_link_layout);
        popupSelectPermissionView = root.findViewById(R.id.popup_select_permission_layout);
        popupSelectPermissionItemView = root.findViewById(R.id.preview_and_download_card);

        passwordProtectionLayout.setVisibility(View.GONE);
        autoExpirationRadioGroup.setVisibility(View.GONE);
        shareCreateLinkLayout.setVisibility(View.GONE);
        shareCopyLinkLayout.setVisibility(View.GONE);

        if (!isDownloadFragment) {
            selectPermissionLayout.setVisibility(View.GONE);
            updatePermissionLayout.setVisibility(View.GONE);
        }
    }

    private void initViewAction() {
        passwordProtectionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                passwordProtectionLayout.setVisibility(isChecked? View.VISIBLE : View.GONE);
            }
        });

        showPasswordImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isShowNewPassword = !isShowNewPassword;
                changeNewPasswordAndImage();
            }
        });

        autoExpirationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                autoExpirationRadioGroup.setVisibility(isChecked? View.VISIBLE : View.GONE);
            }
        });

        autoExpirationRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.expiration_days_rb:
                        expirationDaysRB.setTextColor(getActivity().getResources().getColor(R.color.text_view_color));
                        expirationTimeRB.setTextColor(getActivity().getResources().getColor(R.color.text_view_secondary_color));
                        expirationDaysLayout.setVisibility(View.VISIBLE);
                        expirationTimeLayout.setVisibility(View.GONE);
                        break;
                    case R.id.expiration_time_rb:
                        expirationDaysRB.setTextColor(getActivity().getResources().getColor(R.color.text_view_secondary_color));
                        expirationTimeRB.setTextColor(getActivity().getResources().getColor(R.color.text_view_color));
                        expirationDaysLayout.setVisibility(View.GONE);
                        expirationTimeLayout.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        expirationTimeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectDateTimePopup(true);
                // showDatePickerDialog();
            }
        });

        selectPermissionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPermissionCard.callOnClick();
            }
        });
        selectPermissionCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectPermissionPopup(true);
            }
        });

        copyCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String link = linkText.getText().toString();
                if (!link.isEmpty()) {
                    mShareDialogActivity.showCopyDialog(link);
                }
            }
        });

        linkText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectLinkCard.callOnClick();
            }
        });
        selectLinkCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectLinkPopup();
            }
        });

        qrCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String link = linkText.getText().toString();
                if (!link.isEmpty()) {
                    mShareDialogActivity.showQRDialog(link);
                }
            }
        });

        createCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    CreateShareLinkTask task = new CreateShareLinkTask();
                    ConcurrentAsyncTask.execute(task);
                }
            }
        });

        showCurrentPasswordImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isShowCurrentPassword = !isShowCurrentPassword;
                changeCurrentPasswordAndImage();
            }
        });

        updateAutoExpirationRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.update_expiration_days_rb:
                        updateExpirationDaysRB.setTextColor(getActivity().getResources().getColor(R.color.text_view_color));
                        updateExpirationTimeRB.setTextColor(getActivity().getResources().getColor(R.color.text_view_secondary_color));
                        updateExpirationDaysLayout.setVisibility(View.VISIBLE);
                        updateExpirationTimeLayout.setVisibility(View.GONE);
                        break;
                    case R.id.update_expiration_time_rb:
                        updateExpirationDaysRB.setTextColor(getActivity().getResources().getColor(R.color.text_view_secondary_color));
                        updateExpirationTimeRB.setTextColor(getActivity().getResources().getColor(R.color.text_view_color));
                        updateExpirationDaysLayout.setVisibility(View.GONE);
                        updateExpirationTimeLayout.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        updateExpirationTimeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectDateTimePopup(false);
                // showDatePickerDialog();
            }
        });

        sendCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyCard.callOnClick();
            }
        });

        deleteCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteShareLinkTask task = new DeleteShareLinkTask();
                ConcurrentAsyncTask.execute(task);
            }
        });

        editExpirationImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentExpirationLayout.setVisibility(View.GONE);
                updateExpirationLayout.setVisibility(View.VISIBLE);
            }
        });

        updateExpirationCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateShareLinkTask task = new UpdateShareLinkTask(false, "");
                ConcurrentAsyncTask.execute(task);
            }
        });

        cancelExpirationCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processSeafLink(seafLink);
            }
        });

        updatePermissionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePermissionCard.callOnClick();
            }
        });
        updatePermissionCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectPermissionPopup(false);
            }
        });
    }

    private void init() {
        simpleDateFormatForServer = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        simpleDateFormatForPhone = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        calendar = Calendar.getInstance();

        expirationDaysRB.setChecked(true);
        updateExpirationDaysRB.setChecked(true);
        selectPermissionText.setText(getResources().getString(R.string.preview_and_download));
        updatePermissionText.setText(getResources().getString(R.string.preview_and_download));

        mShareDialogActivity = (ShareDialogActivity)getActivity();

        account = mShareDialogActivity.account;
        dialogType = mShareDialogActivity.dialogType;
        repoID = mShareDialogActivity.repo.getID();
        path = mShareDialogActivity.path;
        fileName = mShareDialogActivity.fileName;
        conn = new SeafConnection(account);

        dataManager = new DataManager(account);

        GetShareLinkTask task = new GetShareLinkTask();
        ConcurrentAsyncTask.execute(task);
    }

    public boolean isTextFile(String fileName) {
        String suffix = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        //file is markdown or  txt
        String[] array = {"doc", "docx", "xlsx", "xls", "pptx", "ppt", "odt", "ods", "odp"};
        boolean flag = Arrays.asList(array).contains(suffix);
        return flag;
    }

    private boolean validateInputs() {
        if (passwordProtectionSwitch.isChecked()) {
            if (passwordEdit.getText().toString().isEmpty()) {
                passwordEdit.setError(getResources().getString(R.string.err_passwd_empty));
                return false;
            }

            int minPwdLength = 8;
            String[] servers = {
                    getString(R.string.server_url_seacloud),
                    getString(R.string.server_url_sync)
            };
            if (Arrays.asList(servers).contains(account.server)) {
                minPwdLength = 4;
            }
            if (passwordEdit.getText().toString().length() < minPwdLength) {
                String error = String.format(
                        getResources().getString(R.string.err_passwd_min_len_limit),
                        minPwdLength
                );
                passwordEdit.setError(error);
                return false;
            }
            if (passwordConfirmEdit.getText().toString().isEmpty()) {
                passwordConfirmEdit.setError(getResources().getString(R.string.err_passwd_confirm_empty));
                return false;
            }
            if (!passwordEdit.getText().toString().equals(passwordConfirmEdit.getText().toString())) {
                passwordEdit.setError((getResources().getString(R.string.err_passwd_mismatch)));
                passwordConfirmEdit.setError(getResources().getString(R.string.err_passwd_mismatch));
                return false;
            }
        }
        if (autoExpirationSwitch.isChecked()) {
            if (expirationDaysRB.isChecked()) {
                if (expirationDaysEdit.getText().toString().length() == 0) {
                    expirationDaysEdit.setError(getResources().getString(R.string.error_days_empty));
                    return false;
                }
            }
            if (expirationTimeRB.isChecked()) {
                if (expirationTimeEdit.getText().toString().length() == 0) {
                    expirationTimeEdit.setError(getResources().getString(R.string.error_time_empty));
                    return false;
                }
            }
        }
        return true;
    }

    private boolean validateUpdateExpirationInputs() {
        if (updateExpirationDaysRB.isChecked()) {
            if (updateExpirationDaysEdit.getText().toString().length() == 0) {
                updateExpirationDaysEdit.setError(getResources().getString(R.string.error_days_empty));
                return false;
            }
        }
        if (updateExpirationTimeRB.isChecked()) {
            if (updateExpirationTimeEdit.getText().toString().length() == 0) {
                updateExpirationTimeEdit.setError(getResources().getString(R.string.error_time_empty));
                return false;
            }
        }
        return true;
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        showTimePickerDialog(year, monthOfYear, dayOfMonth);
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        Calendar currentCalendar = Calendar.getInstance();
//        datePickerDialog.getDatePicker().setMinDate(new Date(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), currentCalendar.get(Calendar.DAY_OF_MONTH)).getTime());
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void showTimePickerDialog(int year,
                                      int monthOfYear, int dayOfMonth) {
        TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Calendar currentCalendar = Calendar.getInstance();
                Calendar selectCalendar = Calendar.getInstance();
                selectCalendar.set(year, monthOfYear, dayOfMonth, hourOfDay, minute);
                if (currentCalendar.getTimeInMillis() > selectCalendar.getTimeInMillis()) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.selection_time_later_current), Toast.LENGTH_SHORT).show();
                    return;
                }
                calendar.set(year, monthOfYear, dayOfMonth, hourOfDay, minute);
                expirationTimeEdit.setText(simpleDateFormatForPhone.format(calendar.getTime()));
                updateExpirationTimeEdit.setText(simpleDateFormatForPhone.format(calendar.getTime()));
            }
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), timePickerListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
        timePickerDialog.show();
    }

    private void showSelectDateTimePopup(boolean isSelect) {

        LayoutInflater mInflater;

        try {

            mInflater = (LayoutInflater) getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = mInflater.inflate(R.layout.popup_select_datetime, null);
            layout.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            layout.measure(View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED);
            mDateTimeDropDown = new PopupWindow(layout, (isSelect? expirationTimeLayout.getWidth() : updateExpirationTimeLayout.getWidth()) + (int) mShareDialogActivity.getResources().getDimension(R.dimen.share_download_expiration_margin_horizontal),
                    popupSelectDatetimeView.getHeight(),true);

            List<String> times = Arrays.asList("00:00", "00:30", "01:00", "01:30", "02:00", "02:30", "03:00", "03:30",
                    "04:00", "04:30", "05:00", "05:30", "06:00", "06:30", "07:00", "07:30",
                    "08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
                    "12:00", "12:30", "13:00", "13:30", "14:00", "14:30", "15:00", "15:30",
                    "16:00", "16:30", "17:00", "17:30", "18:00", "18:30", "19:00", "19:30",
                    "20:00", "20:30", "21:00", "21:30", "22:00", "22:30", "23:00", "23:30");

            final CalendarView calendarView = layout.findViewById(R.id.calendar_view);
            final ListView timeListView = layout.findViewById(R.id.time_list_view);
            final TextView todayText = layout.findViewById(R.id.today_text);
            final ImageView upImage = layout.findViewById(R.id.up_image);
            final ImageView downImage = layout.findViewById(R.id.down_image);

            calendarView.setMinDate(System.currentTimeMillis() + 1000);
            calendarView.setDate(calendar.getTimeInMillis());
            calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
                if (isSelect) {
                    expirationTimeEdit.setText(simpleDateFormatForPhone.format(calendar.getTime()));
                } else {
                    updateExpirationTimeEdit.setText(simpleDateFormatForPhone.format(calendar.getTime()));
                }
            });

            SeafTimeAdapter timeAdapter = new SeafTimeAdapter(mShareDialogActivity);

            timeListView.setAdapter(timeAdapter);
            timeListView.setDivider(null);

            if (selectedTime == null) {
                selectedTime = "12:00";
                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 12, 0);
            }
            timeAdapter.setItems(times, selectedTime);
            timeAdapter.notifyChanged();

            timeListView.smoothScrollToPosition(times.indexOf(selectedTime));
            timeListView.setOnItemClickListener((parent, view, position, id) -> {
                selectedTime = times.get(position);
                timeAdapter.setSelectedTime(selectedTime);
                timeAdapter.notifyChanged();
                String[] separated = selectedTime.split(":");
                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), Integer.parseInt(separated[0]), Integer.parseInt(separated[1]));
                if (isSelect) {
                    expirationTimeEdit.setText(simpleDateFormatForPhone.format(calendar.getTime()));
                } else {
                    updateExpirationTimeEdit.setText(simpleDateFormatForPhone.format(calendar.getTime()));
                }
            });

            todayText.setOnClickListener(v -> {
                calendarView.setDate(System.currentTimeMillis());
                Calendar currentCalendar = Calendar.getInstance();
                calendar.set(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), currentCalendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
                if (isSelect) {
                    expirationTimeEdit.setText(simpleDateFormatForPhone.format(calendar.getTime()));
                } else {
                    updateExpirationTimeEdit.setText(simpleDateFormatForPhone.format(calendar.getTime()));
                }
            });
            upImage.setOnClickListener(v -> {
                timeListView.smoothScrollToPosition(timeListView.getFirstVisiblePosition() - 3);
            });
            downImage.setOnClickListener(v -> {
                timeListView.smoothScrollToPosition(timeListView.getLastVisiblePosition() + 3);
            });

            mDateTimeDropDown.showAsDropDown(isSelect? expirationTimeLayout : updateExpirationTimeLayout, 5, 5);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSelectLinkPopup() {

        LayoutInflater mInflater;

        try {

            mInflater = (LayoutInflater) getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = mInflater.inflate(R.layout.popup_select_link, null);

            layout.measure(View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED);

            final CardView linkPreviewCard = layout.findViewById(R.id.link_preview_card);
            final CardView directDownloadCard = layout.findViewById(R.id.direct_download_card);
            final CardView embeddedWebLinkCard = layout.findViewById(R.id.embedded_web_link_card);
            final TextView linkPreviewText = layout.findViewById(R.id.link_preview_text);
            final TextView directDownloadText = layout.findViewById(R.id.direct_download_text);
            final TextView embeddedWebLinkText = layout.findViewById(R.id.embedded_web_link_text);
            if (linkType.equals(LINK_PREVIEW)) {
                linkPreviewText.setTextColor(mShareDialogActivity.getResources().getColor(R.color.text_view_color));
            } else if (linkType.equals(DIRECT_DOWNLOAD)) {
                directDownloadText.setTextColor(mShareDialogActivity.getResources().getColor(R.color.text_view_color));
            } else if (linkType.equals(EMBEDDED_WEB_LINK)) {
                embeddedWebLinkText.setTextColor(mShareDialogActivity.getResources().getColor(R.color.text_view_color));
            }

            linkPreviewCard.setOnClickListener(view -> {
                linkType = LINK_PREVIEW;
                updateLinkText(seafLink);
                mLinkDropDown.dismiss();
            });
            directDownloadCard.setOnClickListener(view -> {
                linkType = DIRECT_DOWNLOAD;
                updateLinkText(seafLink);
                mLinkDropDown.dismiss();
            });
            embeddedWebLinkCard.setOnClickListener(view -> {
                linkType = EMBEDDED_WEB_LINK;
                updateLinkText(seafLink);
                mLinkDropDown.dismiss();
            });

            int count = 3;
            switch (dialogType) {
                case ShareDialogActivity.SHARE_DIALOG_FOR_REPO:
                case ShareDialogActivity.SHARE_DIALOG_FOR_DIR:
                    directDownloadCard.setVisibility(View.GONE);
                    embeddedWebLinkCard.setVisibility(View.GONE);
                    count = 1;
                    break;
                case ShareDialogActivity.SHARE_DIALOG_FOR_FILE:
//                    embeddedWebLinkCard.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }

//            Drawable background = getResources().getDrawable(android.R.drawable.editbox_dropdown_dark_frame);
//            mPermissionDropDown.setBackgroundDrawable(background);

            mLinkDropDown = new PopupWindow(layout, linkLayout.getWidth(),
                    popupSelectLinkView.getHeight() * count / 3,true);
            mLinkDropDown.showAsDropDown(linkLayout, 5, 5);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSelectPermissionPopup(boolean isSelect) {

        LayoutInflater mInflater;

        try {

            mInflater = (LayoutInflater) getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = mInflater.inflate(R.layout.popup_select_permission, null);

            layout.measure(View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED);

            final CardView previewAndDownloadCard = layout.findViewById(R.id.preview_and_download_card);
            final CardView previewOnlyCard = layout.findViewById(R.id.preview_only_card);
            final CardView downloadAndUploadCard = layout.findViewById(R.id.download_and_upload_card);
            final CardView editOnCloudAndDownloadCard = layout.findViewById(R.id.edit_on_cloud_and_download_card);

            previewAndDownloadCard.setOnClickListener(view -> {
                String permission = getActivity().getResources().getString(R.string.preview_and_download);
                clickedPermissionPopup(permission, isSelect);
            });
            previewOnlyCard.setOnClickListener(view -> {
                String permission = getActivity().getResources().getString(R.string.preview_only);
                clickedPermissionPopup(permission, isSelect);
            });
            downloadAndUploadCard.setOnClickListener(view -> {
                String permission = getActivity().getResources().getString(R.string.download_and_upload);
                clickedPermissionPopup(permission, isSelect);
            });
            editOnCloudAndDownloadCard.setOnClickListener(view -> {
                String permission = getActivity().getResources().getString(R.string.edit_on_cloud_and_download);
                clickedPermissionPopup(permission, isSelect);
            });

            int count = 4;
            switch (dialogType) {
                case ShareDialogActivity.SHARE_DIALOG_FOR_REPO:
                case ShareDialogActivity.SHARE_DIALOG_FOR_DIR:
                    editOnCloudAndDownloadCard.setVisibility(View.GONE);
                    count = 3;
                    break;
                case ShareDialogActivity.SHARE_DIALOG_FOR_FILE:
                    downloadAndUploadCard.setVisibility(View.GONE);
                    count = 3;
                    if (!isTextFile(fileName)) {
                        editOnCloudAndDownloadCard.setVisibility(View.GONE);
                        count = 2;
                    }
                    break;
                default:
                    break;
            }

//            Drawable background = getResources().getDrawable(android.R.drawable.editbox_dropdown_dark_frame);
//            mPermissionDropDown.setBackgroundDrawable(background);
            mPermissionDropDown = new PopupWindow(layout, isSelect? selectPermissionLayout.getWidth() : updatePermissionLayout.getWidth(),
                    popupSelectPermissionView.getHeight() - popupSelectPermissionItemView.getHeight() * (4 - count), true);
            mPermissionDropDown.showAsDropDown(isSelect? selectPermissionLayout : updatePermissionLayout, 5, 5);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clickedPermissionPopup(String permission, boolean isSelect) {
        if (isSelect) {
            selectPermissionText.setText(permission);
        } else {
            UpdateShareLinkTask task = new UpdateShareLinkTask(true, permission);
            ConcurrentAsyncTask.execute(task);
        }
        mPermissionDropDown.dismiss();
    }

    public class GetShareLinkTask extends AsyncTask<Void, Long, Void> {
        private Exception err;

        @Override
        public Void doInBackground(Void... params) {
            mShareDialogActivity.changeProgress(true);
            try {
                //get share link
                ArrayList<SeafLink> shareLinks = new ArrayList<SeafLink>();
                if (isDownloadFragment) {
                    shareLinks = dataManager.getShareLink(repoID, path);
                } else {
                    shareLinks = dataManager.getUploadLink(repoID, path);
                }
                if (shareLinks.size() != 0) {
                    //return to existing link
                    seafLink = shareLinks.get(0);
                }
            } catch (Exception e) {
                err = e;
            }
            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            mShareDialogActivity.changeProgress(false);
            if (err == null) {
                processSeafLink(seafLink);
            } else {
                Toast.makeText(getActivity(), getString(R.string.unknow_error), Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }
    }

    public class DeleteShareLinkTask extends AsyncTask<Void, Long, Void> {
        private Boolean deleteShareLinkResult;
        private Exception err;

        @Override
        public Void doInBackground(Void... params) {
            mShareDialogActivity.changeProgress(true);
            try {
                //get share link
                if (isDownloadFragment) {
                    deleteShareLinkResult = dataManager.deleteShareLink(seafLink.getToken());
                } else {
                    deleteShareLinkResult = dataManager.deleteUploadLink(seafLink.getToken());
                }
            } catch (Exception e) {
                err = e;
            }
            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            mShareDialogActivity.changeProgress(false);
            if (err == null) {
                if (deleteShareLinkResult) {
                    seafLink = null;
                    processSeafLink(seafLink);
                }
            } else {
                Toast.makeText(getActivity(), getString(R.string.unknow_error), Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }
    }

    public class CreateShareLinkTask extends AsyncTask<Void, Long, Void> {
        private Boolean deleteShareLinkResult;
        private Exception err;

        @Override
        public Void doInBackground(Void... params) {
            mShareDialogActivity.changeProgress(true);
            try {
                //creating a Share link
                String password = "";
                String days = "";
                String expirationTime = "";
                String permission = "";

                if (passwordProtectionSwitch.isChecked()) {
                    password = passwordEdit.getText().toString();
                }
                if (autoExpirationSwitch.isChecked()) {
                    if (expirationDaysRB.isChecked()) {
                        days = expirationDaysEdit.getText().toString();
                    }
                    if (expirationTimeRB.isChecked()) {
                        String formattedDate = simpleDateFormatForServer.format(calendar.getTime());
                        expirationTime = formattedDate;
//                        expirationTime = String.format("%d-%d-%dT%d:%d:00", mYear, mMonth + 1, mDay, mHour, mMinute);
                    }
                }

                permission = Utils.permissionNameToJson(getActivity(), selectPermissionText.getText().toString());

                if (isDownloadFragment) {
                    seafLink = conn.createShareLink(repoID, path, password, days, expirationTime, permission);
                } else {
                    seafLink = conn.createUploadLink(repoID, path, password, days, expirationTime);
                }

            } catch (Exception e) {
                err = e;
            }
            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            mShareDialogActivity.changeProgress(false);
            if (err == null) {
                processSeafLink(seafLink);
            } else {
                Toast.makeText(getActivity(), getString(R.string.unknow_error), Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }
    }

    public class UpdateShareLinkTask extends AsyncTask<Void, Long, Void> {
        private Boolean deleteShareLinkResult;
        private Exception err;
        private Boolean isPermissionUpdating;
        private String permissionText;

        public UpdateShareLinkTask(boolean permissionUpdating, String permission) {
            super();
            isPermissionUpdating = permissionUpdating;
            permissionText = permission;
        }

        @Override
        public Void doInBackground(Void... params) {
            mShareDialogActivity.changeProgress(true);
            try {
                //updating a Share link
                String days = "";
                String expirationTime = "";
                String permission = "";
                JSONObject permissionObject = new JSONObject();
                boolean can_edit = false;
                boolean can_download = false;
                boolean can_upload = false;

                if (!isPermissionUpdating) {
                    if (updateExpirationDaysRB.isChecked()) {
                        days = updateExpirationDaysEdit.getText().toString();
                    }
                    if (updateExpirationTimeRB.isChecked()) {
                        String formattedDate = simpleDateFormatForServer.format(calendar.getTime());
                        expirationTime = formattedDate;
//                        expirationTime = String.format("%d-%d-%dT%d:%d:00", mYear, mMonth + 1, mDay, mHour, mMinute);
                    }
                    permission = seafLink.getPermissions();
                } else {
                    permission = Utils.permissionNameToJson(getActivity(), permissionText);
                }

                if (isDownloadFragment) {
                    seafLink = conn.updateShareLink(seafLink.getToken(), days, expirationTime, permission);
                } else {
                    seafLink = conn.updateUploadLink(seafLink.getToken(), days, expirationTime);
                }
            } catch (Exception e) {
                err = e;
            }
            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            mShareDialogActivity.changeProgress(false);
            if (err == null) {
                processSeafLink(seafLink);
            } else {
                Toast.makeText(getActivity(), getString(R.string.unknow_error), Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }
    }

    private void processSeafLink(SeafLink seafLink) {
        if (seafLink != null) {
            isShowNewPassword = false;
            isShowCurrentPassword = false;
            changeNewPasswordAndImage();
            changeCurrentPasswordAndImage();

            shareCreateLinkLayout.setVisibility(View.GONE);
            shareCopyLinkLayout.setVisibility(View.VISIBLE);

            calendar = Calendar.getInstance();
            selectedTime = null;

            updateLinkText(seafLink);

            if (seafLink.getPassword().isEmpty()) {
                currentPasswordCaptionText.setVisibility(View.GONE);
                currentPasswordLayout.setVisibility(View.GONE);
            } else {
                currentPasswordCaptionText.setVisibility(View.VISIBLE);
                currentPasswordLayout.setVisibility(View.VISIBLE);
            }
            if (seafLink.getExpire().isEmpty()) {
                currentExpirationCaptionText.setVisibility(View.GONE);
                currentExpirationLayout.setVisibility(View.GONE);
                updateExpirationLayout.setVisibility(View.GONE);
            } else {
                currentExpirationCaptionText.setVisibility(View.VISIBLE);
                currentExpirationLayout.setVisibility(View.VISIBLE);
                updateExpirationLayout.setVisibility(View.GONE);
                try {
                    calendar.setTime(simpleDateFormatForServer.parse(seafLink.getExpire()));// all done
                    selectedTime = calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
                    currentExpirationText.setText(simpleDateFormatForPhone.format(calendar.getTime()));
                    updateExpirationTimeEdit.setText(simpleDateFormatForPhone.format(calendar.getTime()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if (!seafLink.getPermissions().isEmpty()) {
                updatePermissionText.setText(Utils.permissionJsonToName(getActivity(), seafLink.getPermissions()));
            }
        } else {
            shareCreateLinkLayout.setVisibility(View.VISIBLE);
            shareCopyLinkLayout.setVisibility(View.GONE);
            linkType = LINK_PREVIEW;
        }
    }

    private void updateLinkText(SeafLink seafLink) {
        if (seafLink == null) return;
        if (linkType.equals(LINK_PREVIEW)) {
            linkText.setText(seafLink.getLink());
        } else if (linkType.equals(DIRECT_DOWNLOAD)) {
            linkText.setText(seafLink.getLink() + "?dl=1");
        } else if (linkType.equals(EMBEDDED_WEB_LINK)) {
            linkText.setText(seafLink.getLink() + "?raw=1");
        }
    }

    private void changeNewPasswordAndImage() {
        if (isShowNewPassword) {
            passwordEdit.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            showPasswordImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_eye_open));
        } else {
            passwordEdit.setTransformationMethod(PasswordTransformationMethod.getInstance());
            showPasswordImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_eye_close));
        }
    }

    private void changeCurrentPasswordAndImage() {
        if (isShowCurrentPassword) {
            currentPasswordText.setText(seafLink.getPassword());
            showCurrentPasswordImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_eye_open));
        } else {
            currentPasswordText.setText("********");
            showCurrentPasswordImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_eye_close));
        }
    }
}
