CREATE TABLE IF NOT EXISTS track_playback (
        event_id UUID,
        user_id UInt64,
        track_id UInt64,
        release_id UInt64,
        artist_id UInt64,
        play_time DateTime,
        duration_played UInt32,
        device_type String,
        country_code String,
        session_id UUID
) ENGINE = MergeTree()
ORDER BY (play_time, user_id);

CREATE TABLE IF NOT EXISTS track_like (
        event_id UUID,
        user_id UInt64,
        track_id UInt64,
        release_id UInt64,
        artist_id UInt64,
        like_time DateTime,
        device_type String,
        country_code String,
        session_id UUID
) ENGINE = MergeTree()
ORDER BY (like_time, user_id);

CREATE TABLE IF NOT EXISTS track_add_to_playlist (
        event_id UUID,
        user_id UInt64,
        track_id UInt64,
        playlist_id UInt64,
        release_id UInt64,
        artist_id UInt64,
        add_time DateTime,
        device_type String,
        country_code String,
        session_id UUID
) ENGINE = MergeTree()
ORDER BY (add_time, user_id);