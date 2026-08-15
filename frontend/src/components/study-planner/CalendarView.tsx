/**
 * CalendarView — a simple monthly calendar that highlights dates
 * with study tasks and allows clicking a date to filter the list.
 *
 * @author DevLaunch
 */

import React, { useMemo } from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';

interface CalendarViewProps {
  /** ISO date strings (YYYY-MM-DD) that have tasks. */
  taskDates: string[];
  /** Currently selected date filter, or null. */
  selectedDate: string | null;
  /** Callback when a date is clicked. */
  onDateSelect: (date: string | null) => void;
  /** Currently viewed month (0-indexed month). */
  currentMonth: number;
  /** Currently viewed year. */
  currentYear: number;
  /** Callback to navigate to the previous month. */
  onPrevMonth: () => void;
  /** Callback to navigate to the next month. */
  onNextMonth: () => void;
}

const DAY_NAMES = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

export const CalendarView: React.FC<CalendarViewProps> = ({
  taskDates,
  selectedDate,
  onDateSelect,
  currentMonth,
  currentYear,
  onPrevMonth,
  onNextMonth,
}) => {
  const taskDateSet = useMemo(() => new Set(taskDates), [taskDates]);

  const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();
  const firstDayOfWeek = new Date(currentYear, currentMonth, 1).getDay();

  const todayStr = new Date().toISOString().slice(0, 10);

  const monthName = new Date(currentYear, currentMonth).toLocaleString('default', {
    month: 'long',
    year: 'numeric',
  });

  // Build day cells
  const cells: React.ReactNode[] = [];

  // Empty cells before the first day
  for (let i = 0; i < firstDayOfWeek; i++) {
    cells.push(<div key={`empty-${i}`} className="h-9" />);
  }

  // Day cells
  for (let day = 1; day <= daysInMonth; day++) {
    const dateStr = `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
    const hasTask = taskDateSet.has(dateStr);
    const isSelected = selectedDate === dateStr;
    const isToday = dateStr === todayStr;

    cells.push(
      <button
        key={dateStr}
        onClick={() => onDateSelect(isSelected ? null : dateStr)}
        className={`
          relative flex h-9 w-full items-center justify-center rounded-lg text-sm transition-all
          ${
            isSelected
              ? 'animate-ring-pulse bg-primary-600 font-semibold text-white shadow-sm'
              : isToday
                ? 'bg-primary-50 font-semibold text-primary-700 ring-1 ring-inset ring-primary-200 hover:bg-primary-100'
                : 'text-gray-700 hover:bg-gray-100'
          }
        `}
        title={hasTask ? `${day} — has study tasks` : `${day}`}
      >
        {day}
        {hasTask && (
          <span
            className={`absolute bottom-1 left-1/2 h-1 w-1 -translate-x-1/2 rounded-full ${
              isSelected ? 'bg-white' : 'bg-primary-500'
            }`}
          />
        )}
      </button>,
    );
  }

  return (
    <div className="overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm">
      {/* Gradient month header */}
      <div className="flex items-center justify-between bg-gradient-to-r from-primary-600 to-indigo-600 px-4 py-3">
        <button
          onClick={onPrevMonth}
          className="rounded-lg p-1.5 text-white/80 transition-colors hover:bg-white/15 hover:text-white"
          aria-label="Previous month"
        >
          <ChevronLeft className="h-5 w-5" />
        </button>
        <h3 className="text-sm font-semibold text-white">{monthName}</h3>
        <button
          onClick={onNextMonth}
          className="rounded-lg p-1.5 text-white/80 transition-colors hover:bg-white/15 hover:text-white"
          aria-label="Next month"
        >
          <ChevronRight className="h-5 w-5" />
        </button>
      </div>

      <div className="p-3.5">
        {/* Day name headers */}
        <div className="mb-1 grid grid-cols-7 gap-0">
          {DAY_NAMES.map((name) => (
            <div
              key={name}
              className="py-1 text-center text-[11px] font-semibold uppercase tracking-wider text-gray-400"
            >
              {name}
            </div>
          ))}
        </div>

        {/* Day grid */}
        <div className="grid grid-cols-7 gap-0.5">{cells}</div>
      </div>
    </div>
  );
};
