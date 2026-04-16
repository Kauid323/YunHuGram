package org.telegram.yh.auth;

public class YhLoginViewModel {

    private final YhAuthRepository repository;
    private YhCaptchaInfo captchaInfo;

    public YhLoginViewModel(int account) {
        repository = new YhAuthRepository(account);
    }

    public String getSavedMobile() {
        return repository.getSavedMobile();
    }

    public YhCaptchaInfo getCaptchaInfo() {
        return captchaInfo;
    }

    public void requestCaptcha(YhAuthCallback<YhCaptchaInfo> callback) {
        repository.requestCaptcha(new YhAuthCallback<YhCaptchaInfo>() {
            @Override
            public void onSuccess(YhCaptchaInfo result) {
                captchaInfo = result;
                callback.onSuccess(result);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void requestSmsCode(String mobile, String captchaCode, YhAuthCallback<Void> callback) {
        YhCaptchaInfo localCaptcha = captchaInfo;
        if (localCaptcha == null) {
            callback.onError("请先获取图片验证码");
            return;
        }
        repository.requestSmsCode(mobile, captchaCode, localCaptcha.getId(), callback);
    }

    public void verificationLogin(String mobile, String smsCode, YhAuthCallback<YhLoginSession> callback) {
        repository.verificationLogin(mobile, smsCode, callback);
    }
}
