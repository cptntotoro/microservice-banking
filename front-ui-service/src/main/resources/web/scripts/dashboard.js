document.addEventListener("DOMContentLoaded", () => {
    const buttons = document.querySelectorAll(".dashboard-nav__btn");
    const sections = document.querySelectorAll(".dashboard-section");

    buttons.forEach(btn => {
        btn.addEventListener("click", () => {
            // Снимаем active у кнопок
            buttons.forEach(b => b.classList.remove("active"));
            btn.classList.add("active");

            // Скрываем все секции
            sections.forEach(s => s.classList.remove("active"));

            // Показываем нужную секцию
            const target = document.getElementById(btn.dataset.target);
            if (target) target.classList.add("active");
        });
    });
});