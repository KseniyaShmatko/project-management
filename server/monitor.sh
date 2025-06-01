#!/bin/bash

# Проверка наличия аргумента (имя файла для записи результатов)
if [ $# -ne 1 ]; then
    echo "Использование: $0 <имя_файла_для_результатов>"
    exit 1
fi

OUTPUT_FILE=$1
INTERVAL=5  # интервал в секундах между измерениями

# Очистка файла результатов
echo "timestamp,cpu_usage,memory_used_bytes,memory_total_bytes,memory_usage_percent" > $OUTPUT_FILE

while true; do
    # Получение текущего времени
    TIMESTAMP=$(date +"%Y-%m-%d %H:%M:%S")
    
    # Получение загрузки CPU
    CPU_USAGE=$(top -l 1 -n 0 | grep "CPU usage" | awk '{print $3}' | tr -d '%')
    
    # Получение использования памяти
    MEM_INFO=$(vm_stat | grep "Pages")
    
    # Размер страницы памяти (обычно 4096 байт на macOS)
    PAGE_SIZE=4096
    
    # Расчет используемой памяти
    PAGES_ACTIVE=$(echo "$MEM_INFO" | grep "Pages active" | awk '{print $3}' | tr -d '.')
    PAGES_WIRED=$(echo "$MEM_INFO" | grep "Pages wired down" | awk '{print $4}' | tr -d '.')
    PAGES_COMPRESSED=$(echo "$MEM_INFO" | grep "Pages occupied by compressor" | awk '{print $5}' | tr -d '.')
    
    # Общее количество страниц физической памяти
    PAGES_TOTAL=$(sysctl -n hw.memsize)
    MEM_TOTAL_BYTES=$PAGES_TOTAL
    
    # Используемая память в байтах
    MEM_USED_BYTES=$(( (PAGES_ACTIVE + PAGES_WIRED) * PAGE_SIZE ))
    
    # Процент использования памяти
    MEM_PERCENT=$(echo "scale=2; $MEM_USED_BYTES * 100 / $MEM_TOTAL_BYTES" | bc)
    
    # Запись данных в файл
    echo "$TIMESTAMP,$CPU_USAGE,$MEM_USED_BYTES,$MEM_TOTAL_BYTES,$MEM_PERCENT" >> $OUTPUT_FILE
    
    # Пауза перед следующим измерением
    sleep $INTERVAL
done
