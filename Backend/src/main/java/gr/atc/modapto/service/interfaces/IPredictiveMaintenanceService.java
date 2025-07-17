package gr.atc.modapto.service.interfaces;

import gr.atc.modapto.dto.files.MaintenanceDataDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IPredictiveMaintenanceService {
    void storeCorimData(MultipartFile file);

    List<MaintenanceDataDto> retrieveMaintenanceDataByDateRange(String startDate, String endDate);
}
