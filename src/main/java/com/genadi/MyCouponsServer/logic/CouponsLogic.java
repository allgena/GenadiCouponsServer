package com.genadi.MyCouponsServer.logic;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.genadi.MyCouponsServer.bean.Coupon;
import com.genadi.MyCouponsServer.bean.Purchase;
import com.genadi.MyCouponsServer.dal.ICouponRepository;
import com.genadi.MyCouponsServer.dto.CouponDto;
import com.genadi.MyCouponsServer.enams.CouponCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class CouponsLogic {
    private ICouponRepository couponRepository;
    private PurchasesLogic purchasesLogic;

    @Autowired
    public CouponsLogic(ICouponRepository couponRepository, PurchasesLogic purchasesLogic){
        this.couponRepository=couponRepository;
        this.purchasesLogic = purchasesLogic;
    }


    public Iterable<Coupon> findAll() {
        return couponRepository.findAll();
    }

    public Coupon getById(long id){
        Coupon result = null;
        Optional<Coupon> optionalCoupon = couponRepository.findById(id);
        if (optionalCoupon.isPresent())
            result =  optionalCoupon.get();

        return result;
    }

    public Coupon save(Coupon coupon) {
        return couponRepository.save(coupon);
    }

    public void deleteById(long id) {
        List<Purchase> purchases = purchasesLogic.findByCouponId(id);
        for (Purchase purchase: purchases) {
            purchasesLogic.deleteById(purchase.getId());
        }
        couponRepository.deleteById(id);
    }

    public List<CouponDto> findCouponsDtoByCompanyId(int pageNumber, int amountOfItemsPerPage, long companyId) {
        Pageable pageable = PageRequest.of(pageNumber-1, amountOfItemsPerPage);
        List<Coupon> coupons = couponRepository.findByCompanyId(companyId, pageable).getContent();
        List<CouponDto> result = new ArrayList<>();
        coupons.stream().forEach(c->{
            result.add(new CouponDto(c));
        });
        return result;

    }

    public List<Coupon> findByCompanyId(long companyId) {
        return couponRepository.findByCompanyId(companyId);
    }

    public void deleteByCompanyId(long companyId) {
        couponRepository.deleteByCompanyId(companyId);
    }

    public Iterable<CouponDto> findAllByPage(int pageNumber, int amountOfItemsPerPage) {
        Pageable pageable = PageRequest.of(pageNumber-1, amountOfItemsPerPage);
        List<Coupon> coupons = couponRepository.findAll(pageable).getContent();
        List<CouponDto> result = new ArrayList<>();
        coupons.stream().forEach(c->{
            result.add(new CouponDto(c));
        });
        return result;
    }

    public Coupon findById(long couponId) {
        return couponRepository.findById(couponId).get();
    }

    public List<CouponDto> findCouponsDtoByCategory(String category, int pageNumber, int amountOfItemsPerPage){
        CouponCategory categoryEnum = CouponCategory.valueOf(category.toUpperCase());
        Pageable pageable = PageRequest.of(pageNumber-1, amountOfItemsPerPage);
        List<Coupon> coupons = couponRepository.findByCategory(categoryEnum, pageable).getContent();
        List<CouponDto> result = new ArrayList<>();
        coupons.stream().forEach(c->{
            result.add(new CouponDto(c));
        });
        return result;


    }
    public List<CouponDto> getCouponsByCompanyId(int pageNumber,  int amountOfItemsPerPage, long companyId) throws JsonProcessingException {
        List<CouponDto> coupons = findCouponsDtoByCompanyId(pageNumber, amountOfItemsPerPage, companyId);
        for (CouponDto coupon: coupons){
            Integer numberOfPurchases= purchasesLogic.findPurchaseCountByCouponId(coupon.getCouponId());
            coupon.setNumberOfPurchases(numberOfPurchases);
        }
        return coupons;
    }
}
