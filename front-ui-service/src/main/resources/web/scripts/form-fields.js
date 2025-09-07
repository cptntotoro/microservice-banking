document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('form.form').forEach(initFormValidation);
});

function initFormValidation(form) {
    const submitButton = form.querySelector('button[type="submit"]');
    if (submitButton) submitButton.disabled = true;

    const passwordInput = form.querySelector('[data-validate="password"]');
    const confirmPasswordInput = form.querySelector('[data-validate="confirm-password"]');
    const birthdateInput = form.querySelector('[data-validate="birthdate"]');
    const usernameInput = form.querySelector('[data-validate="username"]');
    const emailInput = form.querySelector('[data-validate="email"]');
    const termsCheckbox = form.querySelector('[data-validate="terms"]');

    const passwordMatchError = form.querySelector('#passwordMatchError');
    const termsError = form.querySelector('#termsError');

    // --- Вспомогательные функции
    function showError(input, message) {
        const errorDiv = input.parentElement.querySelector('.client-error');
        if (errorDiv) {
            errorDiv.textContent = message;
            errorDiv.style.display = 'block';
            input.classList.add('invalid');
            input.classList.remove('valid');
            input.classList.add('shake');
            setTimeout(() => input.classList.remove('shake'), 300);
        }
    }

    function hideError(input) {
        const errorDiv = input.parentElement.querySelector('.client-error');
        if (errorDiv) {
            errorDiv.style.display = 'none';
            input.classList.remove('invalid');
            input.classList.add('valid');
        }
    }

    function validateEmail(email) {
        const latinRegex = /^[a-zA-Z0-9@._-]+$/;
        if (!latinRegex.test(email)) return false;
        const emailRegex = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
        return emailRegex.test(email);
    }

    function validateUsername(username) {
        return /^[a-zA-Z0-9]{5,15}$/.test(username);
    }

    function validateBirthdate(dateString) {
        if (!dateString) return false;
        const date = new Date(dateString);
        const now = new Date();
        const minDate = new Date(1900, 0, 1);
        const maxDate = new Date(now.getFullYear() - 18, now.getMonth(), now.getDate());
        if (isNaN(date.getTime())) return false;
        return date >= minDate && date <= maxDate;
    }

    function checkPasswordStrength(password, form) {
        const strengthBar = form.querySelector('.password-strength__bar');
        const strengthText = form.querySelector('.password-strength__text');
        if (!strengthBar || !strengthText) return;

        let strength = 0;
        if (password.length >= 8) strength++;
        if (/[A-Z]/.test(password)) strength++;
        if (/[0-9]/.test(password)) strength++;
        if (/[^A-Za-z0-9]/.test(password)) strength++;

        const width = (strength / 4) * 100;
        strengthBar.style.width = width + '%';

        switch (strength) {
            case 0:
            case 1:
                strengthBar.style.background = 'var(--error)';
                strengthText.textContent = 'Слабый пароль';
                break;
            case 2:
                strengthBar.style.background = 'var(--warning)';
                strengthText.textContent = 'Средний пароль';
                break;
            case 3:
                strengthBar.style.background = 'var(--ok)';
                strengthText.textContent = 'Хороший пароль';
                break;
            case 4:
                strengthBar.style.background = 'var(--ok)';
                strengthText.textContent = 'Отличный пароль!';
                break;
        }
    }

    function validatePasswords() {
        if (!passwordInput || !confirmPasswordInput) return true;
        const password = passwordInput.value;
        const confirmPassword = confirmPasswordInput.value;
        if (password && confirmPassword && password !== confirmPassword) {
            if (passwordMatchError) passwordMatchError.style.display = 'block';
            confirmPasswordInput.classList.add('invalid');
            return false;
        } else {
            if (passwordMatchError) passwordMatchError.style.display = 'none';
            confirmPasswordInput.classList.remove('invalid');
            return true;
        }
    }

    function validateTerms() {
        if (!termsCheckbox) return true;
        if (!termsCheckbox.checked) {
            if (termsError) termsError.style.display = 'block';
            termsCheckbox.classList.add('invalid');
            return false;
        } else {
            if (termsError) termsError.style.display = 'none';
            termsCheckbox.classList.remove('invalid');
            return true;
        }
    }

    function isFormValid() {
        const requiredInputs = form.querySelectorAll('input[required]');
        for (const input of requiredInputs) {
            if (!input.value.trim()) return false;
        }
        if (emailInput && emailInput.value && !validateEmail(emailInput.value)) return false;
        if (usernameInput && usernameInput.value && !validateUsername(usernameInput.value)) return false;
        if (birthdateInput && birthdateInput.value && !validateBirthdate(birthdateInput.value)) return false;
        if (!validatePasswords()) return false;
        if (!validateTerms()) return false;
        return form.querySelectorAll('.invalid').length === 0;
    }

    function updateSubmitButton() {
        if (submitButton) submitButton.disabled = !isFormValid();
    }

    function validateForm() {
        let isValid = true;
        const requiredInputs = form.querySelectorAll('input[required]');
        requiredInputs.forEach(input => {
            if (!input.value.trim()) {
                showError(input, 'Это поле обязательно для заполнения');
                isValid = false;
            }
        });
        if (emailInput && emailInput.value && !validateEmail(emailInput.value)) {
            showError(emailInput, 'Неверный формат email');
            isValid = false;
        }
        if (usernameInput && usernameInput.value && !validateUsername(usernameInput.value)) {
            showError(usernameInput, 'Логин должен содержать 5-15 символов (только латинские буквы и цифры)');
            isValid = false;
        }
        if (birthdateInput && birthdateInput.value && !validateBirthdate(birthdateInput.value)) {
            showError(birthdateInput, 'Введите корректную дату рождения');
            isValid = false;
        }
        if (!validatePasswords()) isValid = false;
        if (!validateTerms()) isValid = false;
        updateSubmitButton();
        return isValid;
    }

    if (birthdateInput) {
        birthdateInput.addEventListener('input', function () {
            const value = this.value;
            if (value.length > 10) this.value = value.slice(0, 10);
            if (/^\d{4}$/.test(value)) this.value = value + '-';
            else if (/^\d{4}-\d{2}$/.test(value)) this.value = value + '-';
            if (this.value && !validateBirthdate(this.value)) {
                showError(this, 'Введите корректную дату рождения');
            } else {
                hideError(this);
            }
            updateSubmitButton();
        });
        birthdateInput.addEventListener('blur', function () {
            if (this.value && !validateBirthdate(this.value)) {
                showError(this, 'Введите корректную дату рождения');
            } else if (!this.value.trim() && this.hasAttribute('required')) {
                showError(this, 'Это поле обязательно для заполнения');
            } else {
                hideError(this);
            }
            updateSubmitButton();
        });
    }

    if (usernameInput) {
        usernameInput.addEventListener('input', function () {
            this.value = this.value.replace(/[^a-zA-Z0-9]/g, '');
            if (this.value) hideError(this);
            updateSubmitButton();
        });
        usernameInput.addEventListener('blur', function () {
            if (this.value && !validateUsername(this.value)) {
                showError(this, 'Логин должен содержать 5-15 символов (только латинские буквы и цифры)');
            } else if (!this.value.trim() && this.hasAttribute('required')) {
                showError(this, 'Это поле обязательно для заполнения');
            } else {
                hideError(this);
            }
            updateSubmitButton();
        });
    }

    if (emailInput) {
        emailInput.addEventListener('input', function () {
            this.value = this.value.replace(/[^a-zA-Z0-9@._-]/g, '');
            if (this.value && !validateEmail(this.value)) {
                showError(this, 'Неверный формат email');
            } else {
                hideError(this);
            }
            updateSubmitButton();
        });
        emailInput.addEventListener('blur', function () {
            if (this.value && !validateEmail(this.value)) {
                showError(this, 'Неверный формат email');
            } else if (!this.value.trim() && this.hasAttribute('required')) {
                showError(this, 'Это поле обязательно для заполнения');
            } else {
                hideError(this);
            }
            updateSubmitButton();
        });
    }

    if (passwordInput) {
        passwordInput.addEventListener('input', function () {
            checkPasswordStrength(this.value, form);
            validatePasswords();
            if (this.value) hideError(this);
            updateSubmitButton();
        });
    }

    if (confirmPasswordInput) {
        confirmPasswordInput.addEventListener('input', function () {
            validatePasswords();
            updateSubmitButton();
        });
    }

    if (termsCheckbox) {
        termsCheckbox.addEventListener('change', function () {
            validateTerms();
            updateSubmitButton();
        });
    }

    form.querySelectorAll('input').forEach(input => {
        input.addEventListener('blur', function () {
            if (this !== emailInput && this !== birthdateInput && this !== usernameInput) {
                if (!this.value.trim() && this.hasAttribute('required')) {
                    showError(this, 'Это поле обязательно для заполнения');
                } else {
                    hideError(this);
                }
            }
            updateSubmitButton();
        });
        input.addEventListener('input', function () {
            if (this.value.trim()) hideError(this);
            updateSubmitButton();
        });
    });

    form.addEventListener('submit', function (e) {
        if (!validateForm()) {
            e.preventDefault();
            const firstError = form.querySelector('.invalid');
            if (firstError) {
                firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        }
    });

    updateSubmitButton();
}
