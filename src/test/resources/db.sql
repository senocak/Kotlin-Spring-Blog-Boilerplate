SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

--
-- Database: `spring`
--

-- Table structure for table `categories`
CREATE TABLE IF NOT EXISTS `categories` (
    `id` varchar(255) NOT NULL,
    `name` varchar(255) NOT NULL,
    `slug` varchar(255) DEFAULT NULL,
    `image` longtext,
    `created_at` datetime DEFAULT NULL,
    `updated_at` datetime DEFAULT NULL,
    `deleted` bit(1) DEFAULT b'0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table structure for table `comments`
CREATE TABLE IF NOT EXISTS `comments` (
    `id` varchar(255) NOT NULL,
    `body` varchar(255) DEFAULT NULL,
    `email` varchar(255) DEFAULT NULL,
    `name` varchar(255) DEFAULT NULL,
    `post_id` varchar(255) DEFAULT NULL,
    `approved` bit(1) DEFAULT b'0',
    `created_at` datetime DEFAULT NULL,
    `updated_at` datetime DEFAULT NULL,
    `deleted` bit(1) DEFAULT b'0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table structure for table `posts`
CREATE TABLE IF NOT EXISTS `posts` (
    `id` varchar(255) NOT NULL,
    `body` longtext NOT NULL,
    `slug` varchar(255) NOT NULL,
    `title` varchar(255) NOT NULL,
    `user_id` varchar(255) DEFAULT NULL,
    `created_at` datetime DEFAULT NULL,
    `updated_at` datetime DEFAULT NULL,
    `deleted` bit(1) DEFAULT b'0',
    `tags` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table structure for table `post_category`
CREATE TABLE IF NOT EXISTS `post_category` (
    `post_id` varchar(255) NOT NULL,
    `category_id` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table structure for table `roles`
CREATE TABLE IF NOT EXISTS `roles` (
    `id` bigint(20) NOT NULL,
    `name` varchar(60) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table structure for table `users`
CREATE TABLE IF NOT EXISTS `users` (
    `id` varchar(255) NOT NULL,
    `email` varchar(40) DEFAULT NULL,
    `username` varchar(15) DEFAULT NULL,
    `name` varchar(40) DEFAULT NULL,
    `password` varchar(100) DEFAULT NULL,
    `created_at` datetime DEFAULT NULL,
    `updated_at` datetime DEFAULT NULL,
    `deleted` bit(1) DEFAULT b'0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table structure for table `user_roles`
CREATE TABLE IF NOT EXISTS `user_roles` (
    `user_id` varchar(255) NOT NULL,
    `role_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Dumping data for table `roles`
INSERT INTO `roles` (`id`, `name`) VALUES
(2, 'ROLE_ADMIN'),
(1, 'ROLE_USER');

-- Dumping data for table `users`
INSERT INTO `users` (`id`, `email`, `username`, `name`, `password`, `created_at`, `updated_at`, `deleted`) VALUES
('2cb9374e', 'anil1@senocak.com', 'asenocakUser', 'Lucienne', '$2a$10$znsjvm5Y06ZJmpaWGHmmNu4iDJYhk369LR.R3liw2T4RjJcnt9c12', '2022-01-15 16:20:42', '2022-01-15 16:20:42', b'0'),
('3cb9374e', 'anil2@senocak.com', 'asenocakAdmin', 'Kiley', '$2a$10$znsjvm5Y06ZJmpaWGHmmNu4iDJYhk369LR.R3liw2T4RjJcnt9c12', '2022-01-15 16:20:42', '2022-01-15 16:20:42', b'0');

-- Dumping data for table `user_roles`
INSERT INTO `user_roles` (`user_id`, `role_id`) VALUES
('2cb9374e', 1),
('3cb9374e', 2);

-- Dumping data for table `categories`
INSERT INTO `categories` (`id`, `name`, `slug`, `image`, `created_at`, `updated_at`, `deleted`) VALUES
('99ba090bf012', 'Spring Boot', 'spring-boot', 'springbootimageishere', '2022-01-15 16:20:44', '2022-01-15 16:20:44', b'0');
-- Dumping data for table `posts`

INSERT INTO `posts` (`id`, `body`, `slug`, `title`, `user_id`, `created_at`, `updated_at`, `deleted`, `tags`) VALUES
('1cb9374e', 'Quia eum dolor tempore voluptatibus illo ex qui fuga. Distinctio sequi reprehenderit nobis numquam cupiditate. Rerum cupiditate nostrum beatae molestias cum soluta.', 'dynamic-metrics-consultant', 'Dynamic Metrics Consultant', '2cb9374e', '2022-01-15 16:20:46', '2022-01-16 16:11:48', b'0', 'Games;primary');

-- Dumping data for table `post_category`
INSERT INTO `post_category` (`post_id`, `category_id`) VALUES
('1cb9374e', '99ba090bf012');

-- Dumping data for table `comments`
INSERT INTO `comments` (`id`, `body`, `email`, `name`, `post_id`, `approved`, `created_at`, `updated_at`, `deleted`) VALUES
('19284c52', 'After all this time? Always.', 'amparo.von@hotmail.com', 'Karina', '1cb9374e', b'1', '2022-01-15 16:20:52', '2022-01-15 16:20:52', b'0');

-- Indexes for table `categories`
ALTER TABLE `categories`
    ADD PRIMARY KEY (`id`),
    ADD UNIQUE KEY `slug` (`slug`);

-- Indexes for table `comments`
ALTER TABLE `comments`
    ADD PRIMARY KEY (`id`),
    ADD KEY `FKh4c7lvsc298whoyd4w9ta25cr` (`post_id`);

-- Indexes for table `posts`
ALTER TABLE `posts`
    ADD PRIMARY KEY (`id`),
    ADD KEY `FK5lidm6cqbc7u4xhqpxm898qme` (`user_id`);

-- Indexes for table `post_category`
ALTER TABLE `post_category`
    ADD KEY `FKkifam22p4s1nm3bkmp1igcn5w` (`post_id`),
    ADD KEY `FKm6cfovkyqvu5rlm6ahdx3eavj` (`category_id`);

-- Indexes for table `roles`
ALTER TABLE `roles`
    ADD PRIMARY KEY (`id`),
    ADD UNIQUE KEY `name` (`name`);

-- Indexes for table `users`
ALTER TABLE `users`
    ADD PRIMARY KEY (`id`),
    ADD UNIQUE KEY `email` (`email`),
    ADD UNIQUE KEY `username` (`username`);

-- Indexes for table `user_roles`
ALTER TABLE `user_roles`
    ADD PRIMARY KEY (`user_id`,`role_id`),
    ADD KEY `FKh8ciramu9cc9q3qcqiv4ue8a6` (`role_id`);

-- AUTO_INCREMENT for table `roles`
ALTER TABLE `roles`
    MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

-- Constraints for table `comments`
ALTER TABLE `comments`
    ADD CONSTRAINT `FKh4c7lvsc298whoyd4w9ta25cr` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`);

-- Constraints for table `posts`
ALTER TABLE `posts`
    ADD CONSTRAINT `FK5lidm6cqbc7u4xhqpxm898qme` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

-- Constraints for table `post_category`
ALTER TABLE `post_category`
    ADD CONSTRAINT `FKkifam22p4s1nm3bkmp1igcn5w` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`),
    ADD CONSTRAINT `FKm6cfovkyqvu5rlm6ahdx3eavj` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`);

-- Constraints for table `user_roles`
ALTER TABLE `user_roles`
    ADD CONSTRAINT `FKh8ciramu9cc9q3qcqiv4ue8a6` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`),
    ADD CONSTRAINT `FKhfh9dx7w3ubf1co1vdev94g3f` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);
