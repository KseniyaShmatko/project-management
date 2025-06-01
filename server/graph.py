import matplotlib.pyplot as plt
import numpy as np

users = [10, 15, 20, 30, 40, 50, 70, 90, 100, 140, 170, 200]
mean_time = [18.3, 18.7, 19.1, 19.4, 19.7, 19.9, 20.3, 20.7, 21.0, 21.7, 22.2, 22.5]
p95_time = [77.4, 77.5, 77.7, 79.1, 94.6]

# График 1: Среднее время ответа (точечный график)
plt.figure(figsize=(10, 7))

# Основной график - точки
plt.scatter(users, mean_time, color='blue', s=100, label='Среднее время ответа, мс')

# Добавим вертикальные линии от оси X до каждой точки для лучшей визуализации
for i in range(len(users)):
    plt.vlines(x=users[i], ymin=0, ymax=mean_time[i], colors='lightblue', linestyles='dashed', alpha=0.5)

# Добавление аппроксимации (тренд) для визуализации тенденции
# Используем логарифмическую аппроксимацию, так как это хорошо описывает поведение системы
z = np.polyfit(np.log(users), mean_time, 1)
p = np.poly1d(z)
x_for_trendline = np.linspace(min(users), max(users), 100)
y_for_trendline = p(np.log(x_for_trendline))
plt.plot(x_for_trendline, y_for_trendline, 'r--', alpha=0.7, 
         label='Аппроксимация логарифмической функцией')

plt.xlabel('Число пользователей', fontsize=12)
plt.ylabel('Время отклика, мс', fontsize=12)
plt.title("Зависимость среднего времени ответа от числа пользователей", fontsize=14)
plt.grid(True)
plt.legend()

# Добавление значений над точками
for i, (x, y) in enumerate(zip(users, mean_time)):
    plt.annotate(f"{y}", (x, y), textcoords="offset points", xytext=(0,10), ha='center')

# Настройка оси X для показа только фактических значений пользователей
plt.xticks(users)

plt.tight_layout()
plt.savefig("mean_time.pdf", dpi=300)    # Сохраняет как PDF с высоким разрешением
plt.savefig("mean_time.png", dpi=300)    # Также сохраняет как PNG
plt.close()

# # График 2: 95-й перцентиль времени ответа
# plt.figure(figsize=(8,5))
# plt.plot(users, p95_time, 'co-', label='95-й перцентиль, мс')
# plt.xlabel('Число пользователей')
# plt.ylabel('95-й перцентиль отклика, мс')
# plt.title("Зависимость 95-й перцентиля времени отклика от числа пользователей")
# plt.grid(True)
# plt.legend()
# plt.tight_layout()
# plt.savefig("p95_time.pdf")    # Сохраняет как PDF
# plt.close()
