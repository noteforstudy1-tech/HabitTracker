package com.habittracker.app.data.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.habittracker.app.data.dao.HabitCompletionDao;
import com.habittracker.app.data.dao.HabitCompletionDao_Impl;
import com.habittracker.app.data.dao.HabitDao;
import com.habittracker.app.data.dao.HabitDao_Impl;
import com.habittracker.app.data.dao.UserProfileDao;
import com.habittracker.app.data.dao.UserProfileDao_Impl;
import com.habittracker.app.data.dao.WellnessDao;
import com.habittracker.app.data.dao.WellnessDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class HabitDatabase_Impl extends HabitDatabase {
  private volatile HabitDao _habitDao;

  private volatile HabitCompletionDao _habitCompletionDao;

  private volatile WellnessDao _wellnessDao;

  private volatile UserProfileDao _userProfileDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(3) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `habits` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `colorHex` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `frequencyType` TEXT NOT NULL, `customDays` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `habit_completions` (`habitId` INTEGER NOT NULL, `dateEpochDay` INTEGER NOT NULL, `isCompleted` INTEGER NOT NULL, PRIMARY KEY(`habitId`, `dateEpochDay`), FOREIGN KEY(`habitId`) REFERENCES `habits`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE TABLE IF NOT EXISTS `wellness_entries` (`dateEpochDay` INTEGER NOT NULL, `moodIndex` INTEGER NOT NULL, `sleepHours` REAL NOT NULL, PRIMARY KEY(`dateEpochDay`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `user_profile` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `currentStreak` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '95ed2e14ea3c17888186295272cdf40a')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `habits`");
        db.execSQL("DROP TABLE IF EXISTS `habit_completions`");
        db.execSQL("DROP TABLE IF EXISTS `wellness_entries`");
        db.execSQL("DROP TABLE IF EXISTS `user_profile`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsHabits = new HashMap<String, TableInfo.Column>(6);
        _columnsHabits.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHabits.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHabits.put("colorHex", new TableInfo.Column("colorHex", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHabits.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHabits.put("frequencyType", new TableInfo.Column("frequencyType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHabits.put("customDays", new TableInfo.Column("customDays", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysHabits = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesHabits = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoHabits = new TableInfo("habits", _columnsHabits, _foreignKeysHabits, _indicesHabits);
        final TableInfo _existingHabits = TableInfo.read(db, "habits");
        if (!_infoHabits.equals(_existingHabits)) {
          return new RoomOpenHelper.ValidationResult(false, "habits(com.habittracker.app.data.model.Habit).\n"
                  + " Expected:\n" + _infoHabits + "\n"
                  + " Found:\n" + _existingHabits);
        }
        final HashMap<String, TableInfo.Column> _columnsHabitCompletions = new HashMap<String, TableInfo.Column>(3);
        _columnsHabitCompletions.put("habitId", new TableInfo.Column("habitId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHabitCompletions.put("dateEpochDay", new TableInfo.Column("dateEpochDay", "INTEGER", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHabitCompletions.put("isCompleted", new TableInfo.Column("isCompleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysHabitCompletions = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysHabitCompletions.add(new TableInfo.ForeignKey("habits", "CASCADE", "NO ACTION", Arrays.asList("habitId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesHabitCompletions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoHabitCompletions = new TableInfo("habit_completions", _columnsHabitCompletions, _foreignKeysHabitCompletions, _indicesHabitCompletions);
        final TableInfo _existingHabitCompletions = TableInfo.read(db, "habit_completions");
        if (!_infoHabitCompletions.equals(_existingHabitCompletions)) {
          return new RoomOpenHelper.ValidationResult(false, "habit_completions(com.habittracker.app.data.model.HabitCompletion).\n"
                  + " Expected:\n" + _infoHabitCompletions + "\n"
                  + " Found:\n" + _existingHabitCompletions);
        }
        final HashMap<String, TableInfo.Column> _columnsWellnessEntries = new HashMap<String, TableInfo.Column>(3);
        _columnsWellnessEntries.put("dateEpochDay", new TableInfo.Column("dateEpochDay", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWellnessEntries.put("moodIndex", new TableInfo.Column("moodIndex", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWellnessEntries.put("sleepHours", new TableInfo.Column("sleepHours", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysWellnessEntries = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesWellnessEntries = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoWellnessEntries = new TableInfo("wellness_entries", _columnsWellnessEntries, _foreignKeysWellnessEntries, _indicesWellnessEntries);
        final TableInfo _existingWellnessEntries = TableInfo.read(db, "wellness_entries");
        if (!_infoWellnessEntries.equals(_existingWellnessEntries)) {
          return new RoomOpenHelper.ValidationResult(false, "wellness_entries(com.habittracker.app.data.model.WellnessEntry).\n"
                  + " Expected:\n" + _infoWellnessEntries + "\n"
                  + " Found:\n" + _existingWellnessEntries);
        }
        final HashMap<String, TableInfo.Column> _columnsUserProfile = new HashMap<String, TableInfo.Column>(3);
        _columnsUserProfile.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProfile.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProfile.put("currentStreak", new TableInfo.Column("currentStreak", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUserProfile = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUserProfile = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUserProfile = new TableInfo("user_profile", _columnsUserProfile, _foreignKeysUserProfile, _indicesUserProfile);
        final TableInfo _existingUserProfile = TableInfo.read(db, "user_profile");
        if (!_infoUserProfile.equals(_existingUserProfile)) {
          return new RoomOpenHelper.ValidationResult(false, "user_profile(com.habittracker.app.data.model.UserProfile).\n"
                  + " Expected:\n" + _infoUserProfile + "\n"
                  + " Found:\n" + _existingUserProfile);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "95ed2e14ea3c17888186295272cdf40a", "b3efa131d4ecf61c6f9144dc1aa8cf17");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "habits","habit_completions","wellness_entries","user_profile");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `habits`");
      _db.execSQL("DELETE FROM `habit_completions`");
      _db.execSQL("DELETE FROM `wellness_entries`");
      _db.execSQL("DELETE FROM `user_profile`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(HabitDao.class, HabitDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(HabitCompletionDao.class, HabitCompletionDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(WellnessDao.class, WellnessDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(UserProfileDao.class, UserProfileDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public HabitDao habitDao() {
    if (_habitDao != null) {
      return _habitDao;
    } else {
      synchronized(this) {
        if(_habitDao == null) {
          _habitDao = new HabitDao_Impl(this);
        }
        return _habitDao;
      }
    }
  }

  @Override
  public HabitCompletionDao habitCompletionDao() {
    if (_habitCompletionDao != null) {
      return _habitCompletionDao;
    } else {
      synchronized(this) {
        if(_habitCompletionDao == null) {
          _habitCompletionDao = new HabitCompletionDao_Impl(this);
        }
        return _habitCompletionDao;
      }
    }
  }

  @Override
  public WellnessDao wellnessDao() {
    if (_wellnessDao != null) {
      return _wellnessDao;
    } else {
      synchronized(this) {
        if(_wellnessDao == null) {
          _wellnessDao = new WellnessDao_Impl(this);
        }
        return _wellnessDao;
      }
    }
  }

  @Override
  public UserProfileDao userProfileDao() {
    if (_userProfileDao != null) {
      return _userProfileDao;
    } else {
      synchronized(this) {
        if(_userProfileDao == null) {
          _userProfileDao = new UserProfileDao_Impl(this);
        }
        return _userProfileDao;
      }
    }
  }
}
