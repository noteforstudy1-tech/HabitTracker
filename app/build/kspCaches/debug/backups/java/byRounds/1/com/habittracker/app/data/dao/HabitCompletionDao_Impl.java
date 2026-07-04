package com.habittracker.app.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.habittracker.app.data.model.HabitCompletion;
import java.lang.Boolean;
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
public final class HabitCompletionDao_Impl implements HabitCompletionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<HabitCompletion> __insertionAdapterOfHabitCompletion;

  private final SharedSQLiteStatement __preparedStmtOfDeleteCompletion;

  private final SharedSQLiteStatement __preparedStmtOfClearCompletions;

  public HabitCompletionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfHabitCompletion = new EntityInsertionAdapter<HabitCompletion>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `habit_completions` (`habitId`,`dateEpochDay`,`isCompleted`) VALUES (?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final HabitCompletion entity) {
        statement.bindLong(1, entity.getHabitId());
        statement.bindLong(2, entity.getDateEpochDay());
        final int _tmp = entity.isCompleted() ? 1 : 0;
        statement.bindLong(3, _tmp);
      }
    };
    this.__preparedStmtOfDeleteCompletion = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        DELETE FROM habit_completions\n"
                + "        WHERE habitId = ? AND dateEpochDay = ?\n"
                + "    ";
        return _query;
      }
    };
    this.__preparedStmtOfClearCompletions = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM habit_completions";
        return _query;
      }
    };
  }

  @Override
  public Object upsertCompletion(final HabitCompletion completion,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfHabitCompletion.insert(completion);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteCompletion(final long habitId, final long dateEpochDay,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteCompletion.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, habitId);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, dateEpochDay);
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
          __preparedStmtOfDeleteCompletion.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearCompletions(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearCompletions.acquire();
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
          __preparedStmtOfClearCompletions.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<HabitCompletion>> getCompletionsInRange(final long startDay, final long endDay) {
    final String _sql = "\n"
            + "        SELECT * FROM habit_completions\n"
            + "        WHERE dateEpochDay >= ? AND dateEpochDay <= ?\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startDay);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endDay);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"habit_completions"}, new Callable<List<HabitCompletion>>() {
      @Override
      @NonNull
      public List<HabitCompletion> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfHabitId = CursorUtil.getColumnIndexOrThrow(_cursor, "habitId");
          final int _cursorIndexOfDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "dateEpochDay");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final List<HabitCompletion> _result = new ArrayList<HabitCompletion>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final HabitCompletion _item;
            final long _tmpHabitId;
            _tmpHabitId = _cursor.getLong(_cursorIndexOfHabitId);
            final long _tmpDateEpochDay;
            _tmpDateEpochDay = _cursor.getLong(_cursorIndexOfDateEpochDay);
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            _item = new HabitCompletion(_tmpHabitId,_tmpDateEpochDay,_tmpIsCompleted);
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

  @Override
  public Flow<List<DailyCount>> getDailyCompletionCounts(final long startDay, final long endDay) {
    final String _sql = "\n"
            + "        SELECT dateEpochDay, COUNT(*) as count FROM habit_completions\n"
            + "        WHERE dateEpochDay >= ? AND dateEpochDay <= ?\n"
            + "        AND isCompleted = 1\n"
            + "        GROUP BY dateEpochDay\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startDay);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endDay);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"habit_completions"}, new Callable<List<DailyCount>>() {
      @Override
      @NonNull
      public List<DailyCount> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDateEpochDay = 0;
          final int _cursorIndexOfCount = 1;
          final List<DailyCount> _result = new ArrayList<DailyCount>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DailyCount _item;
            final long _tmpDateEpochDay;
            _tmpDateEpochDay = _cursor.getLong(_cursorIndexOfDateEpochDay);
            final int _tmpCount;
            _tmpCount = _cursor.getInt(_cursorIndexOfCount);
            _item = new DailyCount(_tmpDateEpochDay,_tmpCount);
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

  @Override
  public Object isCompleted(final long habitId, final long dateEpochDay,
      final Continuation<? super Boolean> $completion) {
    final String _sql = "\n"
            + "        SELECT EXISTS(\n"
            + "            SELECT 1 FROM habit_completions\n"
            + "            WHERE habitId = ? AND dateEpochDay = ? AND isCompleted = 1\n"
            + "        )\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, habitId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, dateEpochDay);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Boolean>() {
      @Override
      @NonNull
      public Boolean call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Boolean _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp != 0;
          } else {
            _result = false;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
