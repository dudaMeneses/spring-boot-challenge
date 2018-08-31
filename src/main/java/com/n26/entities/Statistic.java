package com.n26.entities;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.util.OptionalDouble;

@Data
public class Statistic {

    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal sum;

    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal avg;

    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal max;

    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal min;

    private long count;

    private Statistic(BigDecimal sum, BigDecimal avg, BigDecimal max, BigDecimal min, long count) {
        this.sum = sum;
        this.avg = avg;
        this.max = max;
        this.min = min;
        this.count = count;
    }

    public static class StatisticBuilder {
        private BigDecimal nestedSum = new BigDecimal(0);
        private BigDecimal nestedAvg = new BigDecimal(0);
        private BigDecimal nestedMax = new BigDecimal(0);
        private BigDecimal nestedMin = new BigDecimal(0);
        private long nestedCount = 0L;

        public StatisticBuilder(){}

        public StatisticBuilder sum(final double sum){
            nestedSum = new BigDecimal(sum).setScale(2, BigDecimal.ROUND_HALF_UP);
            return this;
        }

        public StatisticBuilder avg(final OptionalDouble avg){
            if(avg.isPresent())
                nestedAvg = new BigDecimal(avg.getAsDouble()).setScale(2, BigDecimal.ROUND_HALF_UP);
            else
                nestedAvg = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);

            return this;
        }

        public StatisticBuilder max(final OptionalDouble max){
            if(max.isPresent())
                nestedMax = new BigDecimal(max.getAsDouble()).setScale(2, BigDecimal.ROUND_HALF_UP);
            else
                nestedMax = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);

            return this;
        }

        public StatisticBuilder min(final OptionalDouble min){
            if(min.isPresent())
                nestedMin = new BigDecimal(min.getAsDouble()).setScale(2, BigDecimal.ROUND_HALF_UP);
            else
                nestedMin = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);

            return this;
        }

        public StatisticBuilder count(final long count){
            nestedCount = count;
            return this;
        }

        public Statistic build(){
            return new Statistic(nestedSum, nestedAvg, nestedMax, nestedMin, nestedCount);
        }

    }

}
