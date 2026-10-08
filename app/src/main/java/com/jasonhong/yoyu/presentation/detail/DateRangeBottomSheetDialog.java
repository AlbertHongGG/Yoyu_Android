package com.jasonhong.yoyu.presentation.detail;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.jasonhong.yoyu.databinding.DialogDateRangeBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateRangeBottomSheetDialog extends BottomSheetDialogFragment {

    public interface OnDateRangeSelectedListener {
        void onDateRangeSelected(Date startDate, Date endDate);
    }

    private DialogDateRangeBinding binding;
    private Date startDate;
    private Date endDate;
    private OnDateRangeSelectedListener listener;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

    public static DateRangeBottomSheetDialog newInstance(Date startDate, Date endDate, OnDateRangeSelectedListener listener) {
        DateRangeBottomSheetDialog dialog = new DateRangeBottomSheetDialog();
        dialog.startDate = startDate != null ? startDate : new Date();
        dialog.endDate = endDate != null ? endDate : new Date();
        dialog.listener = listener;
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogDateRangeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateDateDisplay();

        // Quick Tag: 近七天
        binding.tagLast7Days.setOnClickListener(v -> {
            triggerHaptic();
            Calendar now = Calendar.getInstance();
            endDate = now.getTime();
            now.add(Calendar.DAY_OF_YEAR, -7);
            startDate = now.getTime();
            updateDateDisplay();
        });

        // Quick Tag: 近一個月
        binding.tagLastMonth.setOnClickListener(v -> {
            triggerHaptic();
            Calendar now = Calendar.getInstance();
            endDate = now.getTime();
            now.add(Calendar.MONTH, -1);
            startDate = now.getTime();
            updateDateDisplay();
        });

        // Quick Tag: 近三個月
        binding.tagLast3Months.setOnClickListener(v -> {
            triggerHaptic();
            Calendar now = Calendar.getInstance();
            endDate = now.getTime();
            now.add(Calendar.MONTH, -3);
            startDate = now.getTime();
            updateDateDisplay();
        });

        // Quick Tag: 上個三個月
        binding.tagPrev3Months.setOnClickListener(v -> {
            triggerHaptic();
            Calendar now = Calendar.getInstance();
            now.add(Calendar.MONTH, -3);
            endDate = now.getTime();
            now.add(Calendar.MONTH, -3);
            startDate = now.getTime();
            updateDateDisplay();
        });

        // Quick Tag: Q1
        binding.tagQ1.setOnClickListener(v -> {
            triggerHaptic();
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.MONTH, Calendar.JANUARY);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            startDate = cal.getTime();
            cal.set(Calendar.MONTH, Calendar.MARCH);
            cal.set(Calendar.DAY_OF_MONTH, 31);
            endDate = cal.getTime();
            updateDateDisplay();
        });

        // Quick Tag: Q2
        binding.tagQ2.setOnClickListener(v -> {
            triggerHaptic();
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.MONTH, Calendar.APRIL);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            startDate = cal.getTime();
            cal.set(Calendar.MONTH, Calendar.JUNE);
            cal.set(Calendar.DAY_OF_MONTH, 30);
            endDate = cal.getTime();
            updateDateDisplay();
        });

        // Quick Tag: Q3
        binding.tagQ3.setOnClickListener(v -> {
            triggerHaptic();
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.MONTH, Calendar.JULY);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            startDate = cal.getTime();
            cal.set(Calendar.MONTH, Calendar.SEPTEMBER);
            cal.set(Calendar.DAY_OF_MONTH, 30);
            endDate = cal.getTime();
            updateDateDisplay();
        });

        // Quick Tag: Q4
        binding.tagQ4.setOnClickListener(v -> {
            triggerHaptic();
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.MONTH, Calendar.OCTOBER);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            startDate = cal.getTime();
            cal.set(Calendar.MONTH, Calendar.DECEMBER);
            cal.set(Calendar.DAY_OF_MONTH, 31);
            endDate = cal.getTime();
            updateDateDisplay();
        });

        // Custom Pick Start Date
        binding.boxStartDate.setOnClickListener(v -> {
            triggerHaptic();
            PremiumCalendarPickerDialog picker = PremiumCalendarPickerDialog.newInstance(
                    startDate,
                    "選擇起始時間",
                    selectedDate -> {
                        startDate = selectedDate;
                        if (startDate.after(endDate)) {
                            endDate = startDate;
                        }
                        updateDateDisplay();
                    }
            );
            picker.show(getParentFragmentManager(), "PickerStartDate");
        });

        // Custom Pick End Date
        binding.boxEndDate.setOnClickListener(v -> {
            triggerHaptic();
            PremiumCalendarPickerDialog picker = PremiumCalendarPickerDialog.newInstance(
                    endDate,
                    "選擇結束時間",
                    selectedDate -> {
                        endDate = selectedDate;
                        if (endDate.before(startDate)) {
                            startDate = endDate;
                        }
                        updateDateDisplay();
                    }
            );
            picker.show(getParentFragmentManager(), "PickerEndDate");
        });

        // Done button
        binding.btnDoneDateRange.setOnClickListener(v -> {
            triggerHaptic();
            if (listener != null) {
                listener.onDateRangeSelected(startDate, endDate);
            }
            dismiss();
        });
    }

    private void updateDateDisplay() {
        if (binding == null) return;
        binding.tvDateRangeStart.setText(dateFormat.format(startDate));
        binding.tvDateRangeEnd.setText(dateFormat.format(endDate));
    }

    private void triggerHaptic() {
        if (getContext() != null) {
            Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(20);
                }
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return dialog;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
