// document.addEventListener('DOMContentLoaded', function() {
//     const form = document.querySelector('form');
//     const passwordInput = document.getElementById('password');
//     const confirmPasswordInput = document.getElementById('confirmPassword');
//     const termsCheckbox = document.getElementById('agreeTerms');
//     const birthdateInput = document.getElementById('birthdate');
//     const loginInput = document.getElementById('login');
//     const emailInput = document.getElementById('email');
//     const passwordMatchError = document.getElementById('passwordMatchError');
//     const termsError = document.getElementById('termsError');
//     const submitButton = form.querySelector('button[type="submit"]');
//
//     // Деактивация кнопки по умолчанию
//     if (submitButton) {
//         submitButton.disabled = true;
//     }
//
//     // Функция для отображения ошибок
//     function showError(input, message) {
//         // Ищем элемент с классом client-error
//         const errorDiv = input.parentElement.querySelector('.client-error');
//
//         if (errorDiv) {
//             errorDiv.textContent = message;
//             errorDiv.style.display = 'block';
//             input.classList.add('invalid');
//             input.classList.remove('valid');
//             input.classList.add('shake');
//             setTimeout(() => input.classList.remove('shake'), 300);
//         }
//     }
//
// // Функция для скрытия ошибок
//     function hideError(input) {
//         // Ищем элемент с классом client-error
//         const errorDiv = input.parentElement.querySelector('.client-error');
//
//         if (errorDiv) {
//             errorDiv.style.display = 'none';
//             input.classList.remove('invalid');
//             input.classList.add('valid');
//         }
//     }
//
//     // Валидация email
//     function validateEmail(email) {
//         // Проверка на латинские символы
//         const latinRegex = /^[a-zA-Z0-9@._-]+$/;
//         if (!latinRegex.test(email)) {
//             return false;
//         }
//
//         // Проверка формата email
//         const emailRegex = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
//         return emailRegex.test(email);
//     }
//
//     // Валидация логина
//     function validateLogin(login) {
//         const loginRegex = /^[a-zA-Z0-9]{5,15}$/;
//         return loginRegex.test(login);
//     }
//
//     // Валидация даты рождения
//     function validateBirthdate(dateString) {
//         if (!dateString) return false;
//
//         const date = new Date(dateString);
//         const now = new Date();
//         const minDate = new Date(1900, 0, 1);
//         const maxDate = new Date(now.getFullYear() - 18, now.getMonth(), now.getDate()); // Минимум 18 лет
//
//         // Проверка на корректность даты
//         if (isNaN(date.getTime())) {
//             return false;
//         }
//
//         // Проверка диапазона (1900 - текущий год минус 18 лет)
//         return date >= minDate && date <= maxDate;
//     }
//
//     // Проверка сложности пароля
//     function checkPasswordStrength(password) {
//         let strength = 0;
//         const strengthBar = document.querySelector('.password-strength__bar');
//         const strengthText = document.querySelector('.password-strength__text');
//
//         if (!strengthBar || !strengthText) return;
//
//         if (password.length >= 8) strength++;
//         if (/[A-Z]/.test(password)) strength++;
//         if (/[0-9]/.test(password)) strength++;
//         if (/[^A-Za-z0-9]/.test(password)) strength++;
//
//         const width = (strength / 4) * 100;
//         strengthBar.style.width = width + '%';
//
//         switch(strength) {
//             case 0:
//             case 1:
//                 strengthBar.style.background = 'var(--error)';
//                 strengthText.textContent = 'Слабый пароль';
//                 break;
//             case 2:
//                 strengthBar.style.background = 'var(--warning)';
//                 strengthText.textContent = 'Средний пароль';
//                 break;
//             case 3:
//                 strengthBar.style.background = 'var(--ok)';
//                 strengthText.textContent = 'Хороший пароль';
//                 break;
//             case 4:
//                 strengthBar.style.background = 'var(--ok)';
//                 strengthText.textContent = 'Отличный пароль!';
//                 break;
//         }
//     }
//
//     // Валидация паролей
//     function validatePasswords() {
//         const password = passwordInput.value;
//         const confirmPassword = confirmPasswordInput.value;
//
//         if (password && confirmPassword && password !== confirmPassword) {
//             passwordMatchError.style.display = 'block';
//             confirmPasswordInput.classList.add('invalid');
//             return false;
//         } else {
//             passwordMatchError.style.display = 'none';
//             confirmPasswordInput.classList.remove('invalid');
//             return true;
//         }
//     }
//
//     // Валидация условий использования
//     function validateTerms() {
//         if (!termsCheckbox.checked) {
//             termsError.style.display = 'block';
//             termsCheckbox.classList.add('invalid');
//             return false;
//         } else {
//             termsError.style.display = 'none';
//             termsCheckbox.classList.remove('invalid');
//             return true;
//         }
//     }
//
//     // Проверка валидности всей формы
//     function isFormValid() {
//         // Проверка обязательных полей
//         const requiredInputs = form.querySelectorAll('input[required]');
//         for (const input of requiredInputs) {
//             if (!input.value.trim()) {
//                 return false;
//             }
//         }
//
//         // Проверка email
//         if (emailInput.value && !validateEmail(emailInput.value)) {
//             return false;
//         }
//
//         // Проверка логина
//         if (loginInput.value && !validateLogin(loginInput.value)) {
//             return false;
//         }
//
//         // Проверка даты рождения
//         if (birthdateInput.value && !validateBirthdate(birthdateInput.value)) {
//             return false;
//         }
//
//         // Проверка паролей
//         if (!validatePasswords()) {
//             return false;
//         }
//
//         // Проверка условий
//         if (!validateTerms()) {
//             return false;
//         }
//
//         // Проверка, что нет ошибок валидации
//         const hasErrors = form.querySelectorAll('.invalid').length > 0;
//         return !hasErrors;
//     }
//
//     // Обновление состояния кнопки отправки
//     function updateSubmitButton() {
//         if (submitButton) {
//             submitButton.disabled = !isFormValid();
//         }
//     }
//
//     // Валидация всей формы
//     function validateForm() {
//         let isValid = true;
//
//         // Проверка обязательных полей
//         const requiredInputs = form.querySelectorAll('input[required]');
//         requiredInputs.forEach(input => {
//             if (!input.value.trim()) {
//                 showError(input, 'Это поле обязательно для заполнения');
//                 isValid = false;
//             }
//         });
//
//         // Проверка email
//         if (emailInput.value && !validateEmail(emailInput.value)) {
//             showError(emailInput, 'Неверный формат email');
//             isValid = false;
//         }
//
//         // Проверка логина
//         if (loginInput.value && !validateLogin(loginInput.value)) {
//             showError(loginInput, 'Логин должен содержать 5-15 символов (только латинские буквы и цифры)');
//             isValid = false;
//         }
//
//         // Проверка даты рождения
//         if (birthdateInput.value && !validateBirthdate(birthdateInput.value)) {
//             showError(birthdateInput, 'Введите корректную дату рождения');
//             isValid = false;
//         }
//
//         // Проверка паролей
//         if (!validatePasswords()) {
//             isValid = false;
//         }
//
//         // Проверка условий
//         if (!validateTerms()) {
//             isValid = false;
//         }
//
//         updateSubmitButton();
//         return isValid;
//     }
//
//     // Ограничение ввода для даты рождения
//     if (birthdateInput) {
//         birthdateInput.addEventListener('input', function(e) {
//             // Ограничение года 4 цифрами
//             const value = this.value;
//             if (value.length > 10) {
//                 this.value = value.slice(0, 10);
//             }
//
//             // Автоматическое добавление разделителей
//             if (/^\d{4}$/.test(value)) {
//                 this.value = value + '-';
//             } else if (/^\d{4}-\d{2}$/.test(value)) {
//                 this.value = value + '-';
//             }
//
//             // Немедленная валидация при вводе
//             if (this.value && !validateBirthdate(this.value)) {
//                 showError(this, 'Введите корректную дату рождения (минимум 18 лет)');
//             } else {
//                 hideError(this);
//             }
//             updateSubmitButton();
//         });
//
//         birthdateInput.addEventListener('blur', function() {
//             // Сначала проверяем специфические ошибки, потом общие
//             if (this.value && !validateBirthdate(this.value)) {
//                 showError(this, 'Введите корректную дату рождения (минимум 18 лет)');
//             } else if (!this.value.trim() && this.hasAttribute('required')) {
//                 showError(this, 'Это поле обязательно для заполнения');
//             } else {
//                 hideError(this);
//             }
//             updateSubmitButton();
//         });
//     }
//
//     // Обработчики событий для логина
//     if (loginInput) {
//         loginInput.addEventListener('input', function() {
//             // Ограничение ввода только латинскими буквами и цифрами
//             this.value = this.value.replace(/[^a-zA-Z0-9]/g, '');
//
//             if (this.value) {
//                 hideError(this);
//             }
//             updateSubmitButton();
//         });
//
//         loginInput.addEventListener('blur', function() {
//             if (this.value && !validateLogin(this.value)) {
//                 showError(this, 'Логин должен содержать 5-15 символов (только латинские буквы и цифры)');
//             } else if (!this.value.trim() && this.hasAttribute('required')) {
//                 showError(this, 'Это поле обязательно для заполнения');
//             } else {
//                 hideError(this);
//             }
//             updateSubmitButton();
//         });
//     }
//
//     // Обработчики событий для email
//     if (emailInput) {
//         emailInput.addEventListener('input', function() {
//             // Ограничение ввода только латинскими символами, цифрами и разрешенными символами для email
//             this.value = this.value.replace(/[^a-zA-Z0-9@._-]/g, '');
//
//             // Немедленная валидация при вводе
//             if (this.value && !validateEmail(this.value)) {
//                 showError(this, 'Неверный формат email');
//             } else {
//                 hideError(this);
//             }
//             updateSubmitButton();
//         });
//
//         emailInput.addEventListener('blur', function() {
//             // Сначала проверяем специфические ошибки, потом общие
//             if (this.value && !validateEmail(this.value)) {
//                 showError(this, 'Неверный формат email');
//             } else if (!this.value.trim() && this.hasAttribute('required')) {
//                 showError(this, 'Это поле обязательно для заполнения');
//             } else {
//                 hideError(this);
//             }
//             updateSubmitButton();
//         });
//     }
//
//     if (passwordInput) {
//         passwordInput.addEventListener('input', function() {
//             checkPasswordStrength(this.value);
//             validatePasswords();
//             if (this.value) {
//                 hideError(this);
//             }
//             updateSubmitButton();
//         });
//     }
//
//     if (confirmPasswordInput) {
//         confirmPasswordInput.addEventListener('input', function() {
//             validatePasswords();
//             updateSubmitButton();
//         });
//     }
//
//     if (termsCheckbox) {
//         termsCheckbox.addEventListener('change', function() {
//             validateTerms();
//             updateSubmitButton();
//         });
//     }
//
//     // Валидация при изменении полей
//     const inputs = form.querySelectorAll('input');
//     inputs.forEach(input => {
//         input.addEventListener('blur', function() {
//             // Для email и даты рождения уже есть специфические обработчики
//             if (this !== emailInput && this !== birthdateInput && this !== loginInput) {
//                 if (!this.value.trim() && this.hasAttribute('required')) {
//                     showError(this, 'Это поле обязательно для заполнения');
//                 } else {
//                     hideError(this);
//                 }
//             }
//             updateSubmitButton();
//         });
//
//         input.addEventListener('input', function() {
//             if (this.value.trim()) {
//                 hideError(this);
//             }
//             updateSubmitButton();
//         });
//     });
//
//     // Обработка отправки формы
//     form.addEventListener('submit', function(e) {
//         if (!validateForm()) {
//             e.preventDefault();
//
//             // Прокрутка к первой ошибке
//             const firstError = form.querySelector('.invalid');
//             if (firstError) {
//                 firstError.scrollIntoView({
//                     behavior: 'smooth',
//                     block: 'center'
//                 });
//             }
//         }
//     });
//
//     // Инициализация индикатора сложности пароля
//     if (passwordInput) {
//         const formRow = passwordInput.closest('.form__row');
//         if (formRow) {
//             const strengthContainer = document.createElement('div');
//             strengthContainer.className = 'password-strength-container';
//
//             const strengthBarContainer = document.createElement('div');
//             strengthBarContainer.className = 'password-strength';
//
//             const strengthBar = document.createElement('div');
//             strengthBar.className = 'password-strength__bar';
//
//             const strengthText = document.createElement('div');
//             strengthText.className = 'password-strength__text';
//             strengthText.textContent = 'Введите пароль';
//
//             strengthBarContainer.appendChild(strengthBar);
//             strengthContainer.appendChild(strengthBarContainer);
//             strengthContainer.appendChild(strengthText);
//
//             formRow.appendChild(strengthContainer);
//
//             checkPasswordStrength(passwordInput.value);
//         }
//     }
//
//     // Инициализация проверки формы
//     updateSubmitButton();
// });