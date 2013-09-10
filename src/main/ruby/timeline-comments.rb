#!/usr/bin/env ruby
require 'nokogiri'
require 'trollop'
require 'base64'
require 'tokenizer'
require 'set'

pwd = Dir.getwd

opts = Trollop::options do
  banner "Count the number of comments in DaDDs stored in xml files, in a timeline"
  opt :xmls, "The xml/ directory of Bugzillas", :default => './xml/'
  opt :committers, "A listing of Bugzilla commentor IDs who are also committers", :default => 'committer-bugzilla-ids.txt'
  opt :bugs, "A file with the bug IDs to collect", :default => ''
  opt :uniq, "Handle the bug list as unique?", :default => false
end

Trollop::die "xml directory must be a directory" unless Dir.exists?(opts[:xmls])
Trollop::die "bugs file not found!" unless File.exists?(opts[:bugs]) || opts[:bugs].eql?('')
Trollop::die "committer file not found!" unless File.exists?(opts[:committers])

if opts[:bugs].nil? || opts[:bugs].eql?('')
  Dir.chdir(opts[:xmls]); 
  files = Dir.glob("*.xml")
  Dir.chdir(pwd)
else
  files = File.open(opts[:bugs]).readlines.collect {|f| f.strip + ".xml" }
end

# Combine duplicates? 
files.uniq! if opts[:uniq]

# Load committer bugzilla IDs
@committer_bugzillas = File.open(opts[:committers]).readlines.collect{|c| c.strip}

# Initialize table for month => comment_count
@timeline = Hash.new
(0..151).each{|m| @timeline[m]={:comments => 0, :bugs => Set.new}}

def committer? (bugzilla_commentor)
  @committer_bugzillas.include? bugzilla_commentor
end

def parent(file)
  begin
    Nokogiri::XML(File.open("#{file.at_xpath("//dup_id").content}.xml","r"))
  rescue # File doesn't exist?
    nil
  end
end

def read_bug(file)
  xml = Nokogiri::XML(File.open(file,"r"))

  # Go right to the parent bug if this is a duplicate
  while(xml.at_xpath("//resolution").content.eql?("DUPLICATE"))
    xml = parent(xml)
    if xml.nil? #Duplicate of a file that doesn't exist
      $stderr.puts "#{file} leads to a duplicate of a non-HTTPD bug"
      return nil
    end
  end

  # Query the XML for stuff
  id = xml.at_xpath("//bug_id").content.to_i
  comment_dates = xml.xpath("//bug_when")
  
  # Increment the hash for each comment date
  comment_dates.each do |node|
    month = (DateTime.parse(node.content).to_date - Date.new(2001,1,1)).to_i/30 #Difference in 30-day periods
    #puts "#{id}\t#{month}"
    @timeline[month][:comments] = @timeline[month][:comments] +1
    @timeline[month][:bugs] << id
  end

end

Dir.chdir(opts[:xmls]) do 
  files.each do |file|
    read_bug file
  end
end

puts "Period of Time\tNumber of Comments\tNumber of DaDDs"
# Output the timeline data  
@timeline.each do |month, set|
  puts "#{month}\t#{set[:comments]}\t#{set[:bugs].size}"
end


