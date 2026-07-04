package com.habittracker.app.data.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.habittracker.app.data.model.WellnessEntry;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class WellnessDao_Impl implements WellnessDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<WellnessEntry> __insertionAdapterOfWellnessEntry;

  private final SharedSQLiteStatement __preparedStmtOfClearWellness;

  public WellnessDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfWellnessEntry = new EntityInsertionAdapter<WellnessEntry>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `wellness_entries` (`dateEpochDay`,`moodIndex`,`sleepHours`) VALUES (?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WellnessEntry entity) {
        statement.bindLong(1, entity.getDateEpochDay());
        statement.bindLong(2, entity.getMoodIndex());
        statement.bindDouble(3, entity.getSleepHours());
      }
    };
    this.__preparedStmtOfClearWellness = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM wellness_entries";
        return _query;
      }
    };
  }

  @Override
  public Object upsertWellness(final WellnessEntry entry,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfWellnessEntry.insert(entry);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clearWellness(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearWellness.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearWellness.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<WellnessEntry> getWellnessEntry(final long dateEpochDay) {
    final String _sql = "SELECT * FROM wellness_entries WHERE dateEpochDay = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, dateEpochDay);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"wellness_entries"}, new Callable<WellnessEntry>() {
      @Override
      @Nullable
      public WellnessEntry call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "dateEpochDay");
          final int _cursorIndexOfMoodIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "moodIndex");
          final int _cursorIndexOfSleepHours = CursorUtil.getColumnIndexOrThrow(_cursor, "sleepHours");
          final WellnessEntry _result;
          if (_cursor.moveToFirst()) {
            final long _tmpDateEpochDay;
            _tmpDateEpochDay = _cursor.getLong(_cursorIndexOfDateEpochDay);
            final int _tmpMoodIndex;
            _tmpMoodIndex = _cursor.getInt(_cursorIndexOfMoodIndex);
            final float _tmpSleepHours;
            _tmpSleepHours = _cursor.getFloat(_cursorIndexOfSleepHours);
            _result = new WellnessEntry(_tmpDateEpochDay,_tmpMoodIndex,_tmpSleepHours);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<WellnessEntry>> getWellnessInRange(final long startDay, final long endDay) {
    final String _sql = "SELECT * FROM wellness_entries WHERE dateEpochDay >= ? AND dateEpochDay <= ? ORDER BY dateEpochDay ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startDay);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endDay);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"wellness_entries"}, new Callable<List<WellnessEntry>>() {
      @Override
      @NonNull
      public List<WellnessEntry> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "dateEpochDay");
          final int _cursorIndexOfMoodIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "moodIndex");
          final int _cursorIndexOfSleepHours = CursorUtil.getColumnIndexOrThrow(_cursor, "sleepHours");
          final List<WellnessEntry> _result = new ArrayList<WellnessEntry>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WellnessEntry _item;
            final long _tmpDateEpochDay;
            _tmpDateEpochDay = _cursor.getLong(_cursorIndexOfDateEpochDay);
            final int _tmpMoodIndex;
            _tmpMoodIndex = _cursor.getInt(_cursorIndexOfMoodIndex);
            final float _tmpSleepHours;
            _tmpSleepHours = _cursor.getFloat(_cursorIndexOfSleepHours);
            _item = new WellnessEntry(_tmpDateEpochDay,_tmpMoodIndex,_tmpSleepHours);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
