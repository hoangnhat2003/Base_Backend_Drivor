package backend.drivor.base.service.inf;

import backend.drivor.base.domain.document.Account;
import backend.drivor.base.domain.request.ChangePasswordRequest;
import backend.drivor.base.domain.response.ApiResponse;

public interface AccountService {

     ApiResponse changePassword(Account account, ChangePasswordRequest request);

}
