package com.genadi.MyCouponsServer.dal;

import com.genadi.MyCouponsServer.bean.Coupon;
import com.genadi.MyCouponsServer.dto.CouponDto;
import com.genadi.MyCouponsServer.enams.CouponCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICouponRepository extends JpaRepository<Coupon,Long> {


        @Query(value = "select new com.genadi.MyCouponsServer.dto.CouponDto(c.id, c.couponName, c.company.companyName,c.category,c.description, c.price,  c.startDate, c.endDate, 0) from Coupon c " +
                "where c.company.id = :companyId")
        List<CouponDto> findCouponsDtoByCompanyId(long companyId);


        List<Coupon> findByCompanyId(long id);

        void deleteByCompanyId(long companyId);

        Page<Coupon> findByCategory(CouponCategory categoryEnum, Pageable pageable);

        Page<Coupon> findByCompanyId(long companyId, Pageable pageable);
}
