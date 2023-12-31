package com.genadi.MyCouponsServer.logic;

import com.genadi.MyCouponsServer.bean.*;
import com.genadi.MyCouponsServer.dal.ICompanyRepository;
import com.genadi.MyCouponsServer.dal.ICustomerRepository;
import com.genadi.MyCouponsServer.dal.IPurchaseRepository;
import com.genadi.MyCouponsServer.dal.IUserRepository;
import com.genadi.MyCouponsServer.dto.CompanyDto;
import com.genadi.MyCouponsServer.enams.CouponCategory;
import com.genadi.MyCouponsServer.enams.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CompaniesLogic {
    private ICompanyRepository companyRepository;
    private CouponsLogic couponsLogic;
    private IPurchaseRepository purchaseRepository;

    private IUserRepository userRepository;
    private ICustomerRepository customerRepository;
    @Autowired
    public CompaniesLogic(ICustomerRepository customerRepository, ICompanyRepository companyRepository, CouponsLogic couponsLogic, IPurchaseRepository purchaseRepository,IUserRepository userRepository){
        this.companyRepository=companyRepository;
        this.couponsLogic = couponsLogic;
        this.purchaseRepository = purchaseRepository;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
    }



    public Company getById(long id){
        Company result = null;
        Optional<Company> optionalCompany = companyRepository.findById(id);
        if (optionalCompany.isPresent())
         result =  optionalCompany.get();

        return result;
    }

    public CompanyDto findCompanyById(long id) {
        CompanyDto company = companyRepository.findCompanyById(id);
        Integer purchasesCount = purchaseRepository.countCompanyPurchases(company.getCompanyId());
        company.setNumberOfPurchases(purchasesCount);
        return company;
    }

    @Transactional
    public void deleteCompanyById(long companyId){
        if (!companyRepository.findById(companyId).isPresent())
            return;

        List<Coupon> coupons = couponsLogic.findByCompanyId(companyId);
        System.out.printf("Number of coupons found " + coupons.size() );
        for (Coupon coupon: coupons){
            System.out.printf("Deleting purchases for coupon id: " + coupon.getId());
            purchaseRepository.deleteByCouponId(coupon.getId());
        }
        couponsLogic.deleteByCompanyId(companyId);
        userRepository.deleteByCompanyId(companyId);

        companyRepository.deleteById(companyId);
    }

    @Transactional
    public Company createCompanyData(String companyName){
        Company company=companyRepository.save(new Company(companyName, "Comp1 address", "010101"));
        User companyUser = new User(companyName, UserType.COMPANY, "pass", "phoneNumber");
        companyUser.setCompany(company);
        userRepository.save(companyUser);
        Coupon coupon1 = couponsLogic.save(new Coupon("coupon1", CouponCategory.FOOD, 10.0F, "Coupon 1 of "+companyName, Date.valueOf(LocalDate.now()), Date.valueOf(LocalDate.now().plusDays(2)), company));
        Coupon coupon2 = couponsLogic.save(new Coupon("coupon2", CouponCategory.FOOD, 20.0F, "Coupon 2 of "+companyName, Date.valueOf(LocalDate.now()), Date.valueOf(LocalDate.now().plusDays(2)), company));
        coupon1 = couponsLogic.save(coupon1);
        coupon2 = couponsLogic.save(coupon2);

        Customer customerA = customerRepository.findById(1L).get();
        Customer customerB = customerRepository.findById(2L).get();

        purchaseRepository.save(new Purchase(customerA, coupon1,2));
        purchaseRepository.save(new Purchase(customerA, coupon2,2));
        purchaseRepository.save(new Purchase(customerB, coupon1,5));
        purchaseRepository.save(new Purchase(customerB, coupon2,5));
        return company;

    }

    public Company save(Company company) {
        return companyRepository.save(company);
    }

    public List<CompanyDto> findAllCompanies() {
        List<CompanyDto> allCompanies = companyRepository.findAllCompanies();
        for (CompanyDto company: allCompanies){
            Integer purchasesCount = purchaseRepository.countCompanyPurchases(company.getCompanyId());
            company.setNumberOfPurchases(purchasesCount);
        }
        return allCompanies;
    }

    public Iterable<CompanyDto> findAllByPage(int pageNumber, int amountOfItemsPerPage) {
        Pageable pageable = PageRequest.of(pageNumber-1, amountOfItemsPerPage);
        List<CompanyDto> result = new ArrayList<>();
        Page<Company> companies = companyRepository.findAll(pageable);
        for (Company company: companies){
            CompanyDto companyDto = new CompanyDto(company);
            Integer purchasesCount = purchaseRepository.countCompanyPurchases(company.getId());
            companyDto.setNumberOfPurchases(purchasesCount);
            result.add(companyDto);
        }
        return result;
    }

    public Company findCompanyByName(String companyName) {
        return companyRepository.findByCompanyName(companyName);
    }

    public List<String> findAllCompaniesNames() {
       return  companyRepository.findCompaniesNames();
    }
}
