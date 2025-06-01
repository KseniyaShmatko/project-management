import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import os
from datetime import datetime

# Настройка стиля графиков
sns.set(style="whitegrid")
plt.figure(figsize=(12, 10))

# Функция для загрузки и обработки данных мониторинга
def load_and_plot_monitoring_data(file_path, users_count):
    # Загрузка данных
    df = pd.read_csv(file_path)
    
    # Преобразование строковой даты в формат datetime
    df['timestamp'] = pd.to_datetime(df['timestamp'])
    
    # Добавление колонки с относительным временем (в секундах от начала)
    start_time = df['timestamp'].min()
    df['time_seconds'] = (df['timestamp'] - start_time).dt.total_seconds()
    
    # Добавление информации о количестве пользователей
    df['users'] = users_count
    
    return df

# Создаем списки для сбора данных из всех файлов
all_data = []

# Поиск файлов результатов
result_files = [f for f in os.listdir('.') if f.startswith('results_') and f.endswith('users.csv')]

# Обработка каждого файла
for file in result_files:
    # Извлечение количества пользователей из имени файла
    users = int(file.split('_')[1].replace('users.csv', ''))
    
    # Загрузка и обработка данных
    df = load_and_plot_monitoring_data(file, users)
    all_data.append(df)

# Объединение всех данных
if all_data:
    combined_data = pd.concat(all_data)
    
    # Создание нескольких графиков
    plt.figure(figsize=(16, 20))
    
    # 1. График загрузки CPU по времени для разного количества пользователей
    plt.subplot(3, 1, 1)
    for users, group in combined_data.groupby('users'):
        plt.plot(group['time_seconds'], group['cpu_usage'], 
                 label=f'{users} пользователей', linewidth=2)
    
    plt.title('Загрузка CPU при различном количестве пользователей', fontsize=16)
    plt.xlabel('Время (секунды)', fontsize=12)
    plt.ylabel('Загрузка CPU (%)', fontsize=12)
    plt.legend()
    plt.grid(True)
    
    # 2. График использования памяти по времени для разного количества пользователей
    plt.subplot(3, 1, 2)
    for users, group in combined_data.groupby('users'):
        plt.plot(group['time_seconds'], group['memory_usage_percent'], 
                 label=f'{users} пользователей', linewidth=2)
    
    plt.title('Использование памяти при различном количестве пользователей', fontsize=16)
    plt.xlabel('Время (секунды)', fontsize=12)
    plt.ylabel('Использование памяти (%)', fontsize=12)
    plt.legend()
    plt.grid(True)
    
    # 3. Средняя загрузка CPU и памяти в зависимости от количества пользователей
    plt.subplot(3, 1, 3)
    
    # Группировка по количеству пользователей и вычисление средних значений
    avg_by_users = combined_data.groupby('users').agg({
        'cpu_usage': 'mean',
        'memory_usage_percent': 'mean'
    }).reset_index()
    
    # Создание сдвоенных осей для разных масштабов
    ax1 = plt.gca()
    ax2 = ax1.twinx()
    
    # Построение средней загрузки CPU
    line1 = ax1.plot(avg_by_users['users'], avg_by_users['cpu_usage'], 
                     'b-', marker='o', linewidth=2, label='Средняя загрузка CPU')
    ax1.set_xlabel('Количество пользователей', fontsize=12)
    ax1.set_ylabel('Средняя загрузка CPU (%)', color='b', fontsize=12)
    ax1.tick_params(axis='y', labelcolor='b')
    
    # Построение среднего использования памяти
    line2 = ax2.plot(avg_by_users['users'], avg_by_users['memory_usage_percent'], 
                     'r-', marker='s', linewidth=2, label='Среднее использование памяти')
    ax2.set_ylabel('Среднее использование памяти (%)', color='r', fontsize=12)
    ax2.tick_params(axis='y', labelcolor='r')
    
    # Объединение легенд
    lines = line1 + line2
    labels = [l.get_label() for l in lines]
    plt.legend(lines, labels, loc='upper left')
    
    plt.title('Средняя загрузка ресурсов в зависимости от количества пользователей', fontsize=16)
    plt.grid(True)
    
    # Сохранение графиков
    plt.tight_layout()
    plt.savefig('performance_analysis.png', dpi=300)
    print("Графики сохранены в файл 'performance_analysis.png'")
    
    # 4. Создание дополнительного графика для демонстрации эффективности масштабирования
    plt.figure(figsize=(10, 6))
    
    # Нормализация CPU по количеству пользователей (показатель эффективности)
    avg_by_users['efficiency'] = avg_by_users['users'] / avg_by_users['cpu_usage']
    # Нормализация для удобства отображения
    avg_by_users['efficiency_normalized'] = avg_by_users['efficiency'] / avg_by_users['efficiency'].iloc[0]
    
    plt.plot(avg_by_users['users'], avg_by_users['efficiency_normalized'], 
             'g-', marker='D', linewidth=2)
    plt.axhline(y=1, color='r', linestyle='--', alpha=0.5, label='Идеальное масштабирование')
    
    plt.title('Эффективность масштабирования системы', fontsize=16)
    plt.xlabel('Количество пользователей', fontsize=12)
    plt.ylabel('Относительная эффективность', fontsize=12)
    plt.grid(True)
    plt.legend()
    
    plt.tight_layout()
    plt.savefig('scaling_efficiency.png', dpi=300)
    print("График эффективности масштабирования сохранен в файл 'scaling_efficiency.png'")
    
else:
    print("Не найдены файлы с результатами мониторинга.")
