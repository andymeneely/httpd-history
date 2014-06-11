-- phpMyAdmin SQL Dump
-- version 4.0.4
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Jun 11, 2014 at 09:24 PM
-- Server version: 5.5.8-log
-- PHP Version: 5.3.10

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `mailinglist`
--
CREATE DATABASE IF NOT EXISTS `mailinglist` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;
USE `mailinglist`;

-- --------------------------------------------------------

--
-- Table structure for table `email`
--

CREATE TABLE IF NOT EXISTS `email` (
  `messageID` varchar(500) NOT NULL,
  `subject` text,
  `inReplyTo` varchar(500) DEFAULT NULL,
  `repliesCount` int(9) NOT NULL DEFAULT '0',
  `directRepliesCount` int(9) NOT NULL DEFAULT '0',
  `indirectRepliesCount` int(9) NOT NULL DEFAULT '0',
  `respondersCount` int(9) NOT NULL DEFAULT '0',
  `responders` text,
  PRIMARY KEY (`messageID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
