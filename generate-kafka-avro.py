import random
from datetime import datetime, timedelta
from uuid import uuid4
from faker import Faker
from confluent_kafka import Producer

# Инициализация
fake = Faker()

# Настройки Kafka
KAFKA_BOOTSTRAP_SERVERS = 'localhost:9092'

# Топики
TOPICS = {
    'track_playback': 'track_playback',
    'track_like': 'track_like',
    'track_add_to_playlist': 'track_add_to_playlist'
}

# Возможные значения
DEVICE_TYPES = ['mobile', 'desktop', 'web']
COUNTRY_CODES = ['RU', 'US', 'GB', 'DE', 'FR', 'IN']

# Инициализируем Producer
conf = {'bootstrap.servers': KAFKA_BOOTSTRAP_SERVERS}
producer = Producer(conf)

def delivery_report(err, msg):
    """Callback при доставке сообщения"""
    if err:
        print(f'Ошибка доставки: {err}')
    else:
        print(f'Сообщение отправлено в {msg.topic()} [{msg.partition()}]')

def generate_event(event_type):
    """Генерирует событие нужного типа"""
    event = {
        "event_id": str(uuid4()),
        "user_id": random.randint(1, 1000),
        "track_id": random.randint(1, 500),
        "release_id": random.randint(1, 200),
        "artist_id": random.randint(1, 100),
        "device_type": random.choice(DEVICE_TYPES),
        "country_code": random.choice(COUNTRY_CODES),
        "session_id": str(uuid4())
    }

    base_time = fake.date_between(start_date='-1y', end_date='today')
    base_time_dt = datetime.combine(base_time, fake.time_object())
    base_time_str = base_time_dt.isoformat() + "Z"

    if event_type == 'track_playback':
        event['play_time'] = base_time_str
        event['duration_played'] = random.randint(10, 300)  # от 10 до 300 секунд
        return event, TOPICS['track_playback']

    elif event_type == 'track_like':
        like_time = base_time_dt + timedelta(seconds=random.randint(10, 360))
        event['like_time'] = like_time.isoformat() + "Z"
        return event, TOPICS['track_like']

    elif event_type == 'track_add_to_playlist':
        add_time = base_time_dt + timedelta(seconds=random.randint(30, 600))
        event['add_time'] = add_time.isoformat() + "Z"
        event['playlist_id'] = random.randint(1, 50)
        return event, TOPICS['track_add_to_playlist']

def send_events(count=1000):
    for _ in range(count):
        event_type = random.choice(['track_playback', 'track_like', 'track_add_to_playlist'])
        event, topic = generate_event(event_type)

        try:
            producer.produce(
                topic=topic,
                key=event['event_id'],
                value=json.dumps(event),  # сериализуем в JSON
                callback=delivery_report
            )
            producer.poll(0)
        except Exception as e:
            print(f"Ошибка при отправке: {e}")

        time.sleep(0.01)  # эмуляция потока

    producer.flush(timeout=10)
    print("Все сообщения отправлены.")

if __name__ == '__main__':
    print("Генерация и отправка событий в Kafka...")
    send_events(count=1000)
