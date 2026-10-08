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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.jasonhong.yoyu.R;
import com.jasonhong.yoyu.databinding.DialogCalendarPickerBinding;
import com.jasonhong.yoyu.databinding.ItemCalendarDayBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PremiumCalendarPickerDialog extends BottomSheetDialogFragment {

    public interface OnDateSelectedListener {
        void onDateSelected(Date selectedDate);
    }

    private DialogCalendarPickerBinding binding;
    private Date initialDate;
    private String title = "選擇日期";
    private OnDateSelectedListener listener;

    private final Calendar currentMonth = Calendar.getInstance();
    private final Calendar selectedDate = Calendar.getInstance();
    private final Calendar today = Calendar.getInstance();
    private final SimpleDateFormat monthYearFormat = new SimpleDateFormat("yyyy年 M月", Locale.getDefault());

    private CalendarAdapter adapter;

    public static PremiumCalendarPickerDialog newInstance(Date initialDate, String title, OnDateSelectedListener listener) {
        PremiumCalendarPickerDialog dialog = new PremiumCalendarPickerDialog();
        dialog.initialDate = initialDate != null ? initialDate : new Date();
        if (title != null) dialog.title = title;
        dialog.listener = listener;
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogCalendarPickerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (initialDate != null) {
            selectedDate.setTime(initialDate);
            currentMonth.setTime(initialDate);
        }
        currentMonth.set(Calendar.DAY_OF_MONTH, 1);

        binding.tvCalendarTitle.setText(title);

        binding.rvCalendarDays.setLayoutManager(new GridLayoutManager(getContext(), 7));
        adapter = new CalendarAdapter();
        binding.rvCalendarDays.setAdapter(adapter);

        binding.btnPrevMonth.setOnClickListener(v -> {
            triggerHaptic();
            currentMonth.add(Calendar.MONTH, -1);
            refreshMonthView();
        });

        binding.btnNextMonth.setOnClickListener(v -> {
            triggerHaptic();
            currentMonth.add(Calendar.MONTH, 1);
            refreshMonthView();
        });

        binding.btnDoneCalendar.setOnClickListener(v -> {
            triggerHaptic();
            if (listener != null) {
                listener.onDateSelected(selectedDate.getTime());
            }
            dismiss();
        });

        refreshMonthView();
    }

    private void refreshMonthView() {
        binding.tvMonthYear.setText(monthYearFormat.format(currentMonth.getTime()));
        List<DayCell> cells = generateCells(currentMonth);
        adapter.setDays(cells);
    }

    private List<DayCell> generateCells(Calendar monthCal) {
        List<DayCell> list = new ArrayList<>(42);

        Calendar cal = (Calendar) monthCal.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // Sunday = 1, Saturday = 7
        int offset = firstDayOfWeek - Calendar.SUNDAY; // 0..6

        // Previous month days
        Calendar prevMonth = (Calendar) monthCal.clone();
        prevMonth.add(Calendar.MONTH, -1);
        int maxPrevDays = prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = offset - 1; i >= 0; i--) {
            int d = maxPrevDays - i;
            Calendar c = (Calendar) prevMonth.clone();
            c.set(Calendar.DAY_OF_MONTH, d);
            list.add(new DayCell(c, false));
        }

        // Current month days
        int maxCurrentDays = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int d = 1; d <= maxCurrentDays; d++) {
            Calendar c = (Calendar) monthCal.clone();
            c.set(Calendar.DAY_OF_MONTH, d);
            list.add(new DayCell(c, true));
        }

        // Next month filler days (to fill 42 cells)
        Calendar nextMonth = (Calendar) monthCal.clone();
        nextMonth.add(Calendar.MONTH, 1);
        int remaining = 42 - list.size();
        for (int d = 1; d <= remaining; d++) {
            Calendar c = (Calendar) nextMonth.clone();
            c.set(Calendar.DAY_OF_MONTH, d);
            list.add(new DayCell(c, false));
        }

        return list;
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

    private static class DayCell {
        final Calendar date;
        final boolean isCurrentMonth;

        DayCell(Calendar date, boolean isCurrentMonth) {
            this.date = date;
            this.isCurrentMonth = isCurrentMonth;
        }
    }

    private class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayViewHolder> {
        private final List<DayCell> days = new ArrayList<>();

        void setDays(List<DayCell> newDays) {
            days.clear();
            days.addAll(newDays);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemCalendarDayBinding b = ItemCalendarDayBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new DayViewHolder(b);
        }

        @Override
        public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
            DayCell cell = days.get(position);
            int dayNum = cell.date.get(Calendar.DAY_OF_MONTH);
            holder.binding.tvCalendarDay.setText(String.valueOf(dayNum));

            boolean isSelected = isSameDay(cell.date, selectedDate);
            boolean isTodayDate = isSameDay(cell.date, today);

            if (isSelected) {
                holder.binding.tvCalendarDay.setBackgroundResource(R.drawable.bg_calendar_selected);
                holder.binding.tvCalendarDay.setTextColor(Color.WHITE);
            } else if (isTodayDate) {
                holder.binding.tvCalendarDay.setBackgroundResource(R.drawable.bg_calendar_today);
                holder.binding.tvCalendarDay.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary));
            } else {
                holder.binding.tvCalendarDay.setBackground(null);
                if (!cell.isCurrentMonth) {
                    holder.binding.tvCalendarDay.setTextColor(Color.parseColor("#44888888"));
                } else {
                    int attrColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary_light);
                    holder.binding.tvCalendarDay.setTextColor(attrColor);
                }
            }

            holder.itemView.setOnClickListener(v -> {
                triggerHaptic();
                selectedDate.setTime(cell.date.getTime());

                // Auto-switch month if tapped outside current month
                if (cell.date.before(currentMonth)) {
                    currentMonth.add(Calendar.MONTH, -1);
                    refreshMonthView();
                } else {
                    Calendar endOfMonth = (Calendar) currentMonth.clone();
                    endOfMonth.set(Calendar.DAY_OF_MONTH, endOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH));
                    if (cell.date.after(endOfMonth)) {
                        currentMonth.add(Calendar.MONTH, 1);
                        refreshMonthView();
                    } else {
                        notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return days.size();
        }

        private boolean isSameDay(Calendar a, Calendar b) {
            return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                    && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
        }

        class DayViewHolder extends RecyclerView.ViewHolder {
            final ItemCalendarDayBinding binding;

            DayViewHolder(ItemCalendarDayBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
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
