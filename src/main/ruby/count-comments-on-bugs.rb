#!/usr/bin/env ruby
require 'csv'
require 'mechanize'

a = Mechanize.new

CSV.foreach("bugs-2013-06-18.csv") do | row |
  bug_id = row[0].to_i
  table = {}
  #puts "Downloading bug #{bug_id}..."
  a.get("https://issues.apache.org/bugzilla/show_bug.cgi?ctype=xml&id=#{bug_id}") do | page |
    puts "#{bug_id}\t#{page.search("//commentid").size}"
    File.open("./xml/#{bug_id}.xml","w+"){ |file| file.write(page.body) }
  end
end

p "Done!"
