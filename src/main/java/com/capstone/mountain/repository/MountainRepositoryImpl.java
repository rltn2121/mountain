package com.capstone.mountain.repository;

import com.capstone.mountain.domain.Mountain;
import com.capstone.mountain.dto.MountainMainPageDto;
import com.capstone.mountain.dto.MountainPreviewDto;
import com.capstone.mountain.dto.QMountainMainPageDto;
import com.capstone.mountain.dto.QMountainPreviewDto;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.LocalDateTime;
import java.util.List;

import static com.capstone.mountain.domain.QMountain.mountain;

@RequiredArgsConstructor
public class MountainRepositoryImpl implements MountainRepositoryCustom{
    private final JPAQueryFactory queryFactory;
    @Override
    public List<MountainMainPageDto> getHotMountain() {
        return queryFactory
                .select(
                        new QMountainMainPageDto(
                                mountain.thumbnail,
                                mountain.id,
                                mountain.name
                        )
                )
                .from(mountain)
                .orderBy(orderCond())
                .limit(5)
                .fetch();
    }

    @Override
    public List<MountainMainPageDto> getNearMountain(double latitude, double longitude) {
        List<MountainMainPageDto> fetch = queryFactory
                .select(
                        new QMountainMainPageDto(
                                mountain.thumbnail,
                                mountain.id,
                                mountain.name
                        ))
                .from(mountain)
                .where(test(
                        latitude,
                        longitude,
                        mountain.latitude.doubleValue().toString(),
                        mountain.longitude.doubleValue().toString())
                )
                .limit(5)
                .fetch();

        return fetch;
    }

    @Override
    public Page<MountainPreviewDto> getMountainList(Pageable pageable) {
        List<MountainPreviewDto> content = queryFactory
                .select(
                        new QMountainPreviewDto(
                                mountain.id,
                                mountain.name,
                                mountain.height,
                                mountain.location,
                                mountain.thumbnail
                        ))
                .from(mountain)
                .orderBy(orderCond())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Mountain> countQuery = queryFactory
                .selectFrom(mountain);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
    }

    private OrderSpecifier<?> orderCond() {
        int month = LocalDateTime.now().getMonthValue();
        if(month >= 3 && month <= 5) {
            return mountain.spring.asc();
        }
        else if(month >= 6 && month <= 8) {
            return mountain.summer.asc();
        }
        else if(month >= 9 && month <= 11) {
            return mountain.fall.asc();
        }
        else {
            return mountain.winter.asc();
        }

    }


    public BooleanExpression test(double lat, double lon, String dstLat, String dstLon){
//        System.out.println("dstLat = " + dstLat);
//        System.out.println("dstLon = " + dstLon);
//        double dist = measure(lat, lon, dstLat, dstLon);
        BooleanExpression ret = Expressions.asBoolean(true);
//        return (dist <= 50000)? ret.isTrue() : ret.isFalse();
        return ret.isTrue();
    }
    private double measure(double lat1, double lon1, double lat2, double lon2){
        double R = 6378.137; // Radius of earth in KM
        double dLat = lat2 * Math.PI / 180 - lat1 * Math.PI / 180;
        double dLon = lon2 * Math.PI / 180 - lon1 * Math.PI / 180;
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c;
        return d * 1000; // meters
    }
}
