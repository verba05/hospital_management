package hospital_managment.service;

import hospital_managment.domain.Hospital;
import hospital_managment.domain.Doctor;
import hospital_managment.patterns.UnitOfWorkContext;
import hospital_managment.patterns.Query;
import hospital_managment.repository.HospitalRepository;
import java.util.List;

public class HospitalService {

    public Hospital getHospitalById(int id) {
        HospitalRepository hospitalRepo = (HospitalRepository) UnitOfWorkContext.getRegistry().getRepository(Hospital.class);
        Query query = new Query()
            .where("id", Query.Operator.EQUALS, id)
            .limit(1);
        
        List<Hospital> hospitals = hospitalRepo.find(query, UnitOfWorkContext.getIdentityMap(), UnitOfWorkContext.getCurrent().getConnection());
        return hospitals.isEmpty() ? null : hospitals.get(0);
    }

    public List<Hospital> getAllHospitals() {
        HospitalRepository hospitalRepo = (HospitalRepository) UnitOfWorkContext.getRegistry().getRepository(Hospital.class);
        Query query = new Query();
        return hospitalRepo.find(query, UnitOfWorkContext.getIdentityMap(), UnitOfWorkContext.getCurrent().getConnection());
    }

    public List<Hospital> searchHospitals(String searchQuery) {
        HospitalRepository hospitalRepo = (HospitalRepository) UnitOfWorkContext.getRegistry().getRepository(Hospital.class);
        
        Query nameQuery = new Query()
            .where("hospital_name", Query.Operator.LIKE, "%" + searchQuery + "%");
        List<Hospital> results = hospitalRepo.find(nameQuery, UnitOfWorkContext.getIdentityMap(), UnitOfWorkContext.getCurrent().getConnection());
        
        Query cityQuery = new Query()
            .where("city", Query.Operator.LIKE, "%" + searchQuery + "%");
        List<Hospital> cityResults = hospitalRepo.find(cityQuery, UnitOfWorkContext.getIdentityMap(), UnitOfWorkContext.getCurrent().getConnection());
        
        Query streetQuery = new Query()
            .where("street_address", Query.Operator.LIKE, "%" + searchQuery + "%");
        List<Hospital> streetResults = hospitalRepo.find(streetQuery, UnitOfWorkContext.getIdentityMap(), UnitOfWorkContext.getCurrent().getConnection());
        
        Query countryQuery = new Query()
            .where("country", Query.Operator.LIKE, "%" + searchQuery + "%");
        List<Hospital> countryResults = hospitalRepo.find(countryQuery, UnitOfWorkContext.getIdentityMap(), UnitOfWorkContext.getCurrent().getConnection());
        
        results.addAll(cityResults);
        results.addAll(streetResults);
        results.addAll(countryResults);
        
        return results;
    }

    public List<Hospital> searchHospitalsByMultipleCriteria(String name, String city, String streetAddress, String stateProvince, String country) {
        HospitalRepository hospitalRepo = (HospitalRepository) UnitOfWorkContext.getRegistry().getRepository(Hospital.class);
        Query query = new Query();
        
        if (name != null && !name.trim().isEmpty()) {
            query.where("hospital_name", Query.Operator.LIKE, "%" + name.trim() + "%");
        }
        if (city != null && !city.trim().isEmpty()) {
            query.where("city", Query.Operator.LIKE, "%" + city.trim() + "%");
        }
        if (streetAddress != null && !streetAddress.trim().isEmpty()) {
            query.where("street_address", Query.Operator.LIKE, "%" + streetAddress.trim() + "%");
        }
        if (stateProvince != null && !stateProvince.trim().isEmpty()) {
            query.where("state_province", Query.Operator.LIKE, "%" + stateProvince.trim() + "%");
        }
        if (country != null && !country.trim().isEmpty()) {
            query.where("country", Query.Operator.LIKE, "%" + country.trim() + "%");
        }
        
        return hospitalRepo.find(query, UnitOfWorkContext.getIdentityMap(), UnitOfWorkContext.getCurrent().getConnection());
    }

    public void updateHospital(Hospital hospital) {
        UnitOfWorkContext.getCurrent().registerDirty(hospital);
    }

    public void deleteHospital(Hospital hospital) {
        UnitOfWorkContext.getCurrent().registerRemoved(hospital);
    }

    public List<Doctor> getDoctorsByHospital(int hospitalId) {
        Hospital hospital = getHospitalById(hospitalId);
        return hospital != null ? hospital.getDoctors() : List.of();
    }

    public List<Doctor> findDoctorsBySpecialty(int hospitalId, String specialty) {
        Hospital hospital = getHospitalById(hospitalId);
        return hospital != null ? hospital.findDoctorsBySpecialty(specialty) : List.of();
    }
}
