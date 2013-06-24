#!/usr/bin/env ruby
require 'csv'
require 'mechanize'
require 'ruby-progressbar'

a = Mechanize.new

#bugfile = "allbugs.csv"
bugfile = "dadd-random.txt"

num_files = File.open(bugfile).readlines.size

puts "Downloading #{num_files} files into xml/\nThis could take about #{num_files/20} minutes.\n"
progress = ProgressBar.create(:starting_at => 0, :total => num_files)

counter = 0

CSV.foreach(bugfile) do | row |
  bug_id = row[0].to_i
  #print "Downloading bug #{bug_id}..."
  a.get("https://issues.apache.org/bugzilla/show_bug.cgi?ctype=xml&id=#{bug_id}") do | page |
    File.open("./sample-xmls/#{'%04d'%counter}-#{bug_id}.xml","w+"){ |file| file.write(page.body) }
    counter+=1
  end
  progress.increment
end

