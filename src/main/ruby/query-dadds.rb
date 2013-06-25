#!/usr/bin/env ruby
require 'nokogiri'
require 'trollop'
require 'base64'

pwd = Dir.getwd

opts = Trollop::options do
  banner "Count the number of comments in Bugzlla bugs stored in xml files"
  opt :xmls, "The xml/ directory of Bugzillas", :default => './xml/'
  opt :bugs, "A file with the bug IDs to collect", :default => ''
  opt :min_comments, "The minimum number of comments to show", :default => 0
  opt :min_commentors, "The minimum number of unique commentators on the bug", :default => 0
end

Trollop::die "xml directory must be a directory" unless Dir.exists?(opts[:xmls])
Trollop::die "bugs file not found!" unless File.exists?(opts[:bugs]) || opts[:bugs].eql?('')
Trollop::die "min-comments must be 0 or greater" unless opts[:min_comments]>=0
Trollop::die "min-commentors must be 0 or greater" unless opts[:min_commentors]>=0

if opts[:bugs].nil? || opts[:bugs].eql?('')
  Dir.chdir(opts[:xmls]); 
  files = Dir.glob("*.xml")
  Dir.chdir(pwd)
else
  files = File.open(opts[:bugs]).readlines.collect {|f| f.strip + ".xml" }
  puts files
end

Dir.chdir(opts[:xmls])

files.each do |file|
  xml = Nokogiri::XML(File.open(file,"r"))

  # Pull the comment IDs and commentors
  comment_ids = xml.xpath("//commentid")
  commentor_nodes = xml.xpath("//who")
  commentors = []
  commentor_nodes.each { |c| commentors << c.content }

  # Count the number of comments not made by the reporter
  non_reporter_comments = 0
  reporter = xml.at_xpath("//reporter").content
  commentor_nodes.each { |c| non_reporter_comments+=1 if !reporter.eql?(c.content) }

  commentors.uniq!

  next if commentors.size < opts[:min_commentors] || comment_ids.size < opts[:min_comments]

  # Query the XML for stuff
  id = xml.at_xpath("//bug_id").content.to_i

  patches = xml.xpath("//attachment[@ispatch=1]")
  votes = xml.at_xpath("//votes").content.to_i
  priority = xml.at_xpath("//priority").content
  severity = xml.at_xpath("//bug_severity").content
  status = xml.at_xpath("//bug_status").content
  resolution = xml.at_xpath("//resolution").content
  comments = xml.xpath("//thetext")


  # Count the number of files for each patch
  patch_files = []
  patches.each do |node|
    Base64.decode64(node.at_xpath("//data").content).each_line do |line| 
      patch_files << line.split("\t")[0].split(" ")[1] if line.start_with? "+++ "
    end
  end
  patch_files.uniq!

  # Count the number of comments that were a reply to another comment
  replies = 0
  comments.each{|c| replies +=1 if c.content.include? "(In reply to comment"}

  # Did any comments mention RFC?
  mention_rfc = "No"
  comments.each{|c| mention_rfc = "Yes" if c.content.downcase.include? "rfc"}

  # Print to console!
  puts "#{id}\t#{comment_ids.size}\t#{commentors.size}\t#{votes}\t#{patches.size}\t#{patch_files.size}\t#{replies}\t#{non_reporter_comments}\t#{mention_rfc}\t#{priority}\t#{severity}\t#{status}\t#{resolution}"

end

puts "ID\tCommments\tCommentors\tVotes\tPatches\tFiles in Patches\tReplies\tNon-Reporter Comments\tMention RFC?\tPriority\tSeverity\tStatus\tResolution"

