package wooteco.auth.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import wooteco.auth.domain.Token;
import wooteco.exception.badRequest.ErrorResponse;
import wooteco.exception.badRequest.PasswordIncorrectException;
import wooteco.exception.notFound.MemberNotFoundException;
import wooteco.exception.unauthorized.InvalidTokenException;
import wooteco.auth.infrastructure.JwtTokenProvider;
import wooteco.auth.dao.MemberDao;
import wooteco.auth.domain.Member;
import wooteco.exception.unauthorized.LoginFailException;

@Service
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberDao memberDao;

    public AuthService(JwtTokenProvider jwtTokenProvider, MemberDao memberDao) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberDao = memberDao;
    }

    public Token login(String email, String password) {
        Member member = memberDao.findByEmail(email).orElseThrow(LoginFailException::new);
        if(member.invalidPassword(password)) {
            //TODO : errorResponse 에 정확한 에러 정보 넘겨주기
            throw new PasswordIncorrectException(new ErrorResponse());
        }
        return new Token(jwtTokenProvider.createToken(member.getId().toString()));
    }

    public Member findMemberWithToken(String accessToken) {
        if (!jwtTokenProvider.validateToken(accessToken)) {
            throw new InvalidTokenException();
        }
        Long id = Long.valueOf(jwtTokenProvider.getPayload(accessToken));
        return memberDao.findById(id).orElseThrow(MemberNotFoundException::new);
    }
}
